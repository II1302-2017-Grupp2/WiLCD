/**
  ******************************************************************************
  * File Name          : USART.c
  * Description        : This file provides code for the configuration
  *                      of the USART instances.
  ******************************************************************************
  *
  * COPYRIGHT(c) 2017 STMicroelectronics
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *   1. Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *   2. Redistributions in binary form must reproduce the above copyright notice,
  *      this list of conditions and the following disclaimer in the documentation
  *      and/or other materials provided with the distribution.
  *   3. Neither the name of STMicroelectronics nor the names of its contributors
  *      may be used to endorse or promote products derived from this software
  *      without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  ******************************************************************************
  */

/* Includes ------------------------------------------------------------------*/
#include "usart.h"

#include "gpio.h"
#include "dma.h"

/* USER CODE BEGIN 0 */

#include "i2c.h"
#include <string.h>

/* Should define WIFI_SSID and WIFI_PSK
 * which contain your SSID and Key respectively */
#include "secrets.h"

uint8_t espReceiveBuffer[ESP_BUF_SIZE];
uint16_t espReceiveBufferLo = 0;
uint16_t espReceiveBufferHi = 0;

uint8_t uart1Buf[2];
uint8_t uart1Pos = 0;
uint8_t uart2Buf[2];
uint8_t uart2Pos = 0;

char *espReadyMsg = "ready";
char *espOkMsg = "OK";
char *espErrorMsg = "ERROR";
char *espFailMsg = "FAIL";
char *espIpDataMsg = "+IPD,";

int8_t ESP_WaitForMsg(char *msg, uint8_t loop);

void ESP_Init() {
	uart1Pos = 0;
	uart2Pos = 0;
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_9, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_11, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_13, GPIO_PIN_SET); // ESP GPIO0 LOW = reprogramming mode
	HAL_Delay(2000);
	HAL_UART_Receive_DMA(&huart1, uart1Buf, 2);
	HAL_UART_Receive_DMA(&huart2, uart2Buf, 2);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_11, GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_9, GPIO_PIN_SET);

	UART_DebugLog("Wi-Fi Booting");
	uint8_t bootOk = 0;
	for (int i = 0; i < 400; i++) {
		if (ESP_WaitForMsg(espReadyMsg, 0) == 1) {
			bootOk = 1;
			break;
		}
		HAL_Delay(10);
	}
	if (bootOk == 0) {
		UART_DebugLog("Wi-Fi Boot Timeout, Resetting MCU...");
		HAL_Delay(1000);
		HAL_NVIC_SystemReset();
	}
	/*UART_DebugLog("Wi-Fi Resetting");
	ESP_SendCommand("AT+RESTORE"); // Factory reset
	ESP_WaitForMsg(espReadyMsg, 1);*/
	ESP_SendCommand("AT+RFPOWER=5"); // Send ASAP to prevent brownout
	UART_DebugLog("Wi-Fi Configuring");
	ESP_SendCommand("AT");
	ESP_SendCommand("ATE0"); // Disable command echo
	ESP_SendCommand("AT+CIPMUX=0");
	ESP_SendCommand("AT+CWMODE_CUR=1");

	UART_DebugLog("Wi-Fi Connecting");
	if (ESP_SendCommand("AT+CWJAP_CUR=\""WIFI_SSID"\",\""WIFI_PSK"\"") == 0) {
		UART_DebugLog("Wi-Fi Failed");
		HAL_NVIC_SystemReset();
	}
	ESP_SendCommand("AT+SLEEP=1"); // Allow sleeping
	UART_DebugLog("Wi-Fi TCP Connecting");
	if (ESP_SendCommand("AT+CIPSTART=\"TCP\",\"10.254.254.1\",9797") == 0) {
		UART_DebugLog("Wi-Fi TCP Failed");
		HAL_NVIC_SystemReset();
	}
	UART_DebugLog("Wi-Fi Connected");
}

uint16_t ESP_ReadLine(uint8_t *buf) {
	int j = 0;
	uint8_t eolFound = 0;
	for (uint16_t i = espReceiveBufferLo; i != espReceiveBufferHi; i = (i + 1) % ESP_BUF_SIZE) {
		if (espReceiveBuffer[i] == '\r' || espReceiveBuffer[i] == '\n') {
			eolFound = 1;
			espReceiveBufferLo = (i + 1) % sizeof(espReceiveBuffer);
		} else if (eolFound) {
			break;
		} else {
			buf[j] = espReceiveBuffer[i];
			++j;
		}
	}
	if (eolFound) {
		if (j == 6 && strncmp((char *)buf, "CLOSED", j) == 0) {
			UART_DebugLog("ESP DISCONNECTED, RESETTING MCU...");
			HAL_NVIC_SystemReset();
		}
		return j;
	} else {
		return 0;
	}
}

int8_t ESP_WaitForMsg(char *msg, uint8_t loop) {
	int msgLen = strlen(msg);
	int errLen = strlen(espErrorMsg);
	int failLen = strlen(espFailMsg);
	uint8_t buf[ESP_BUF_SIZE];
	do {
		int recvLen = ESP_ReadLine(buf);
		if (recvLen >= 1 && buf[0] == '+') {
			HAL_UART_Transmit(&huart1, buf, recvLen, 1000);
			UART_DebugLog("");
		}
		if (recvLen == msgLen && strncmp(msg, (char *)buf, msgLen) == 0) {
			return 1;
		}
		if ((recvLen == errLen && strncmp(espErrorMsg, (char *)buf, errLen) == 0)
				|| (recvLen == failLen && strncmp(espFailMsg, (char *)buf, failLen) == 0)) {
			HAL_UART_Transmit(&huart1, buf, recvLen, 1000);
			UART_DebugLog("");
			return 0;
		}
	} while(loop);
	return -1;
}

int8_t ESP_WaitForOk() {
	return ESP_WaitForMsg(espOkMsg, 1);
}

int8_t ESP_SendCommand(char *msg) {
	HAL_UART_Transmit(&huart2, (uint8_t *)msg, strlen(msg), 1000);
	HAL_UART_Transmit(&huart2, (uint8_t *)"\r\n", 2, 1000);
	return ESP_WaitForOk();
}

int16_t ESP_TCP_ReadLine(uint8_t *buf) {
	uint8_t headerBuf[strlen(espIpDataMsg)+1];
	uint16_t headerLen = 0;
	uint16_t i = espReceiveBufferLo;
	for (; i != espReceiveBufferHi && headerLen < 5; i = (i + 1) % ESP_BUF_SIZE) {
		if (espReceiveBuffer[i] == '\r' || espReceiveBuffer[i] == '\n') {
			continue;
		}

		headerBuf[headerLen] = espReceiveBuffer[i];
		++headerLen;
	}
	headerBuf[headerLen] = 0;
	if (headerLen >= 1 && headerBuf[0] != '+') {
		return -1;
	}
	if (headerLen != 5 || strncmp(espIpDataMsg, (char *)headerBuf, strlen(espIpDataMsg)) != 0) {
		return 0;
	}
	uint16_t len = 0;
	for (; i != espReceiveBufferHi && espReceiveBuffer[i] != ':'; i = (i + 1) % ESP_BUF_SIZE) {
		len = (len * 10) + (espReceiveBuffer[i] - '0');
	}
	if (i == espReceiveBufferHi) {
		return 0;
	}
	++i;
	uint16_t copied = 0;
	for (; i != espReceiveBufferHi && copied < len; i = (i + 1) % ESP_BUF_SIZE) {
		buf[copied] = espReceiveBuffer[i];
		++copied;
	}
	if (copied == len) {
		espReceiveBufferLo = i;
		return len;
	} else {
		return 0;
	}
}

void ESP_SleepUntilMessage() {
	if (espReceiveBufferHi == espReceiveBufferLo) {
		HAL_PWR_EnterSLEEPMode(PWR_MAINREGULATOR_ON, PWR_SLEEPENTRY_WFE);
		__WFI();
		HAL_Delay(200);
	}
}

void UART_DebugLog(char *msg) {
	HAL_UART_Transmit(&huart1, (uint8_t *)msg, strlen(msg), 1000);
	HAL_UART_Transmit(&huart1, (uint8_t *)"\r\n", 2, 1000);
	Display_Str(msg);
}

void HAL_UART_RxHalfCpltCallback(UART_HandleTypeDef *huart) {
	HAL_UART_RxCpltCallback(huart);
}

void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart) {
	__HAL_UART_SEND_REQ(huart, UART_RXDATA_FLUSH_REQUEST);
	uint8_t value = 0;
	if (huart->Instance == USART1) {
		value = uart1Buf[uart1Pos];
		uart1Pos = (uart1Pos + 1) % 2;
		HAL_UART_Transmit(&huart2, &value, 1, 1000);
		return;
	} else if (huart->Instance == USART2) {
		value = uart2Buf[uart2Pos];
		uart2Pos = (uart2Pos + 1) % 2;
	}
	espReceiveBuffer[espReceiveBufferHi] = value;
	espReceiveBufferHi = (espReceiveBufferHi + 1) % ESP_BUF_SIZE;
}

/* USER CODE END 0 */

UART_HandleTypeDef huart1;
UART_HandleTypeDef huart2;
DMA_HandleTypeDef hdma_usart1_rx;
DMA_HandleTypeDef hdma_usart2_rx;

/* USART1 init function */

void MX_USART1_UART_Init(void)
{

  huart1.Instance = USART1;
  huart1.Init.BaudRate = 9600;
  huart1.Init.WordLength = UART_WORDLENGTH_8B;
  huart1.Init.StopBits = UART_STOPBITS_1;
  huart1.Init.Parity = UART_PARITY_NONE;
  huart1.Init.Mode = UART_MODE_TX_RX;
  huart1.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart1.Init.OverSampling = UART_OVERSAMPLING_16;
  huart1.Init.OneBitSampling = UART_ONE_BIT_SAMPLE_DISABLE;
  huart1.AdvancedInit.AdvFeatureInit = UART_ADVFEATURE_NO_INIT;
  if (HAL_UART_Init(&huart1) != HAL_OK)
  {
    Error_Handler();
  }

}
/* USART2 init function */

void MX_USART2_UART_Init(void)
{

  huart2.Instance = USART2;
  huart2.Init.BaudRate = 115200;
  huart2.Init.WordLength = UART_WORDLENGTH_8B;
  huart2.Init.StopBits = UART_STOPBITS_1;
  huart2.Init.Parity = UART_PARITY_NONE;
  huart2.Init.Mode = UART_MODE_TX_RX;
  huart2.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart2.Init.OverSampling = UART_OVERSAMPLING_16;
  huart2.Init.OneBitSampling = UART_ONE_BIT_SAMPLE_DISABLE;
  huart2.AdvancedInit.AdvFeatureInit = UART_ADVFEATURE_NO_INIT;
  if (HAL_UART_Init(&huart2) != HAL_OK)
  {
    Error_Handler();
  }

}

void HAL_UART_MspInit(UART_HandleTypeDef* uartHandle)
{

  GPIO_InitTypeDef GPIO_InitStruct;
  if(uartHandle->Instance==USART1)
  {
  /* USER CODE BEGIN USART1_MspInit 0 */

  /* USER CODE END USART1_MspInit 0 */
    /* Peripheral clock enable */
    __HAL_RCC_USART1_CLK_ENABLE();
  
    /**USART1 GPIO Configuration    
    PC4     ------> USART1_TX
    PC5     ------> USART1_RX 
    */
    GPIO_InitStruct.Pin = Debug_UART_TX_Pin|Debug_UART_RX_Pin;
    GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
    GPIO_InitStruct.Pull = GPIO_PULLUP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
    GPIO_InitStruct.Alternate = GPIO_AF7_USART1;
    HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

    /* Peripheral DMA init*/
  
    hdma_usart1_rx.Instance = DMA1_Channel5;
    hdma_usart1_rx.Init.Direction = DMA_PERIPH_TO_MEMORY;
    hdma_usart1_rx.Init.PeriphInc = DMA_PINC_DISABLE;
    hdma_usart1_rx.Init.MemInc = DMA_MINC_ENABLE;
    hdma_usart1_rx.Init.PeriphDataAlignment = DMA_PDATAALIGN_BYTE;
    hdma_usart1_rx.Init.MemDataAlignment = DMA_MDATAALIGN_BYTE;
    hdma_usart1_rx.Init.Mode = DMA_CIRCULAR;
    hdma_usart1_rx.Init.Priority = DMA_PRIORITY_LOW;
    if (HAL_DMA_Init(&hdma_usart1_rx) != HAL_OK)
    {
      Error_Handler();
    }

    __HAL_LINKDMA(uartHandle,hdmarx,hdma_usart1_rx);

  /* USER CODE BEGIN USART1_MspInit 1 */

  /* USER CODE END USART1_MspInit 1 */
  }
  else if(uartHandle->Instance==USART2)
  {
  /* USER CODE BEGIN USART2_MspInit 0 */

  /* USER CODE END USART2_MspInit 0 */
    /* Peripheral clock enable */
    __HAL_RCC_USART2_CLK_ENABLE();
  
    /**USART2 GPIO Configuration    
    PB3     ------> USART2_TX
    PB4     ------> USART2_RX 
    */
    GPIO_InitStruct.Pin = ESP_UART_TX_Pin|ESP_UART_RX_Pin;
    GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
    GPIO_InitStruct.Pull = GPIO_PULLUP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
    GPIO_InitStruct.Alternate = GPIO_AF7_USART2;
    HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);

    /* Peripheral DMA init*/
  
    hdma_usart2_rx.Instance = DMA1_Channel6;
    hdma_usart2_rx.Init.Direction = DMA_PERIPH_TO_MEMORY;
    hdma_usart2_rx.Init.PeriphInc = DMA_PINC_DISABLE;
    hdma_usart2_rx.Init.MemInc = DMA_MINC_ENABLE;
    hdma_usart2_rx.Init.PeriphDataAlignment = DMA_PDATAALIGN_BYTE;
    hdma_usart2_rx.Init.MemDataAlignment = DMA_MDATAALIGN_BYTE;
    hdma_usart2_rx.Init.Mode = DMA_CIRCULAR;
    hdma_usart2_rx.Init.Priority = DMA_PRIORITY_LOW;
    if (HAL_DMA_Init(&hdma_usart2_rx) != HAL_OK)
    {
      Error_Handler();
    }

    __HAL_LINKDMA(uartHandle,hdmarx,hdma_usart2_rx);

  /* USER CODE BEGIN USART2_MspInit 1 */

  /* USER CODE END USART2_MspInit 1 */
  }
}

void HAL_UART_MspDeInit(UART_HandleTypeDef* uartHandle)
{

  if(uartHandle->Instance==USART1)
  {
  /* USER CODE BEGIN USART1_MspDeInit 0 */

  /* USER CODE END USART1_MspDeInit 0 */
    /* Peripheral clock disable */
    __HAL_RCC_USART1_CLK_DISABLE();
  
    /**USART1 GPIO Configuration    
    PC4     ------> USART1_TX
    PC5     ------> USART1_RX 
    */
    HAL_GPIO_DeInit(GPIOC, Debug_UART_TX_Pin|Debug_UART_RX_Pin);

    /* Peripheral DMA DeInit*/
    HAL_DMA_DeInit(uartHandle->hdmarx);
  /* USER CODE BEGIN USART1_MspDeInit 1 */

  /* USER CODE END USART1_MspDeInit 1 */
  }
  else if(uartHandle->Instance==USART2)
  {
  /* USER CODE BEGIN USART2_MspDeInit 0 */

  /* USER CODE END USART2_MspDeInit 0 */
    /* Peripheral clock disable */
    __HAL_RCC_USART2_CLK_DISABLE();
  
    /**USART2 GPIO Configuration    
    PB3     ------> USART2_TX
    PB4     ------> USART2_RX 
    */
    HAL_GPIO_DeInit(GPIOB, ESP_UART_TX_Pin|ESP_UART_RX_Pin);

    /* Peripheral DMA DeInit*/
    HAL_DMA_DeInit(uartHandle->hdmarx);
  /* USER CODE BEGIN USART2_MspDeInit 1 */

  /* USER CODE END USART2_MspDeInit 1 */
  }
} 

/* USER CODE BEGIN 1 */

/* USER CODE END 1 */

/**
  * @}
  */

/**
  * @}
  */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
