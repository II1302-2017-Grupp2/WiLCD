/**
  ******************************************************************************
  * File Name          : main.h
  * Description        : This file contains the common defines of the application
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
/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __MAIN_H
#define __MAIN_H
  /* Includes ------------------------------------------------------------------*/

/* USER CODE BEGIN Includes */

/* USER CODE END Includes */

/* Private define ------------------------------------------------------------*/

#define ESP_RESET_Pin GPIO_PIN_5
#define ESP_RESET_GPIO_Port GPIOA
#define ESP_ENABLE_Pin GPIO_PIN_6
#define ESP_ENABLE_GPIO_Port GPIOA
#define ESP_GPIO2_Pin GPIO_PIN_7
#define ESP_GPIO2_GPIO_Port GPIOA
#define EPAPER_BORDER_CTRL_Pin GPIO_PIN_0
#define EPAPER_BORDER_CTRL_GPIO_Port GPIOB
#define EPAPER_CS_Pin GPIO_PIN_10
#define EPAPER_CS_GPIO_Port GPIOB
#define EPAPER_BUSY_Pin GPIO_PIN_11
#define EPAPER_BUSY_GPIO_Port GPIOB
#define EPAPER_FLASH_CS_Pin GPIO_PIN_12
#define EPAPER_FLASH_CS_GPIO_Port GPIOB
#define EPAPER_RESET_Pin GPIO_PIN_13
#define EPAPER_RESET_GPIO_Port GPIOB
#define EPAPER_POWER_Pin GPIO_PIN_14
#define EPAPER_POWER_GPIO_Port GPIOB
#define EPAPER_DISCHARGE_Pin GPIO_PIN_15
#define EPAPER_DISCHARGE_GPIO_Port GPIOB
#define ESP_GPIO0_Pin GPIO_PIN_8
#define ESP_GPIO0_GPIO_Port GPIOA
#define ESP_UART_TX_Pin GPIO_PIN_9
#define ESP_UART_TX_GPIO_Port GPIOA
#define ESP_UART_RX_Pin GPIO_PIN_10
#define ESP_UART_RX_GPIO_Port GPIOA

/* USER CODE BEGIN Private defines */

/* USER CODE END Private defines */

/**
  * @}
  */ 

/**
  * @}
*/ 

#endif /* __MAIN_H */
/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
