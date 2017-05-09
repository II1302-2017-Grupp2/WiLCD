#include "epaper.h"
#include "spi.h"

#define EPAPER_LINE_BUFFER_SIZE (1 + 33 + 44 + 33)

typedef struct {
	uint8_t border_byte;
	uint8_t even[33];
	uint8_t scan[44];
	uint8_t odd[33];
} epaperLineData;

typedef union {
	epaperLineData data;
	uint8_t uint8[EPAPER_LINE_BUFFER_SIZE];
} epaperLine;

#define EPAPER_COLS (264)
#define EPAPER_LINES (176)
#define EPAPER_8LINES (EPAPER_LINES/8)

uint8_t epaperScreenBuffer[EPAPER_8LINES][EPAPER_COLS];

epaperLine epaperLineBuffer;

void Epaper_Init() {
	HAL_GPIO_WritePin(EPAPER_FLASH_CS_GPIO_Port, EPAPER_FLASH_CS_Pin, GPIO_PIN_SET);
	HAL_GPIO_WritePin(EPAPER_POWER_GPIO_Port, EPAPER_POWER_Pin, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(EPAPER_DISCHARGE_GPIO_Port, EPAPER_DISCHARGE_Pin, GPIO_PIN_RESET);

	HAL_GPIO_WritePin(EPAPER_POWER_GPIO_Port, EPAPER_POWER_Pin, GPIO_PIN_SET);

	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_SET);
	HAL_GPIO_WritePin(EPAPER_RESET_GPIO_Port, EPAPER_RESET_Pin, GPIO_PIN_SET);
	HAL_Delay(10);
	HAL_GPIO_WritePin(EPAPER_RESET_GPIO_Port, EPAPER_RESET_Pin, GPIO_PIN_RESET);
	HAL_Delay(10);
	HAL_GPIO_WritePin(EPAPER_RESET_GPIO_Port, EPAPER_RESET_Pin, GPIO_PIN_SET);
	HAL_Delay(10);

	for (int i = 0; i < EPAPER_LINE_BUFFER_SIZE; i++) {
		epaperLineBuffer.uint8[i] = 0;
	}

	while (HAL_GPIO_ReadPin(EPAPER_BUSY_GPIO_Port, EPAPER_BUSY_Pin) == GPIO_PIN_SET);

	// Check COG id
	if ((Epaper_Read_ID() & 0x0f) != 0x02) {
		Error_Handler();
	}

	// Disable OE
	Epaper_Send_Byte(0x02, 0x40);

	// Check breakage
	if ((Epaper_Read(0x0F, 0x00) & 0x80) != 0x80) {
		Error_Handler();
	}

	// Power Saving Mode
	Epaper_Send_Byte(0x0B, 0x02);

	// Channel Select
	Epaper_Send(0x01, ((uint8_t[]){
		0x00, 0x00,
		0x00, 0x7F,
		0xFF, 0xFE,
		0x00, 0x00
	}), 8);

	// Oscillator Select
	Epaper_Send_Byte(0x07, 0xD1);
	// Power Setting
	Epaper_Send_Byte(0x08, 0x02);
	//Vcom Level
	Epaper_Send_Byte(0x09, 0xC2);
	// Power Setting
	Epaper_Send_Byte(0x04, 0x03);
	// Driver Latch On
	Epaper_Send_Byte(0x03, 0x01);
	// Driver Latch Off
	Epaper_Send_Byte(0x03, 0x00);
	HAL_Delay(10);

	// Charge Pump
	for (int i = 0; i < 4; ++i) {
		// Positive Voltage
		Epaper_Send_Byte(0x05, 0x01);
		HAL_Delay(240);
		// Negative Voltage
		Epaper_Send_Byte(0x05, 0x03);
		HAL_Delay(100);
		// Vcom on
		Epaper_Send_Byte(0x05, 0x0F);
		HAL_Delay(50);

		if ((Epaper_Read(0x0F, 0x00) & 0x40) == 0x40) {
			// Output enable to disable
			Epaper_Send_Byte(0x02, 0x06);
			Epaper_Clear();
			Epaper_Write_StrLine(2, "Hello, There");
			return;
		}
	}

	// Not charging :(
	Error_Handler();
}

void Epaper_Shutdown() {
	// TODO Nothing Frame
	// TODO Dummy Line

	Epaper_Send_Byte(0x0B, 0x00);
	// Latch Reset
	Epaper_Send_Byte(0x03, 0x01);
	// Chargepump Off
	Epaper_Send_Byte(0x05, 0x03);
	// Chargepump Negative Off
	Epaper_Send_Byte(0x05, 0x01);
	HAL_Delay(400);

	// Internal Discharge
	Epaper_Send_Byte(0x04, 0x80);
	// Chargepump Positive Off
	Epaper_Send_Byte(0x05, 0x00);
	// Oscillator off
	Epaper_Send_Byte(0x07, 0x01);
	HAL_Delay(50);

	HAL_GPIO_WritePin(EPAPER_POWER_GPIO_Port, EPAPER_POWER_Pin, GPIO_PIN_RESET);
	HAL_Delay(20);
	HAL_GPIO_WritePin(EPAPER_RESET_GPIO_Port, EPAPER_RESET_Pin, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_RESET);

	HAL_GPIO_WritePin(EPAPER_DISCHARGE_GPIO_Port, EPAPER_DISCHARGE_Pin, GPIO_PIN_SET);
	HAL_Delay(200);
	HAL_GPIO_WritePin(EPAPER_DISCHARGE_GPIO_Port, EPAPER_DISCHARGE_Pin, GPIO_PIN_RESET);
}

void _Epaper_Transmit_Byte(uint8_t msg) {
	HAL_SPI_Transmit(&hspi1, &msg, 1, 1000);
}

void _Epaper_Blink_CS() {
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_SET);
	HAL_Delay(1);
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_RESET);
}

uint8_t Epaper_Read(uint8_t reg, uint8_t data) {
	HAL_Delay(1);
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_RESET);
	_Epaper_Transmit_Byte(0x70); // Register Index
	_Epaper_Transmit_Byte(reg);

	_Epaper_Blink_CS();

	_Epaper_Transmit_Byte(0x73); // Register Read
	uint8_t value = 0;
	HAL_SPI_TransmitReceive(&hspi1, &data, &value, 1, 1000);

	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_SET);
	return value;
}

uint8_t Epaper_Read_ID() {
	HAL_Delay(1);
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_RESET);
	_Epaper_Transmit_Byte(0x71);
	uint8_t value = 0;
	HAL_SPI_Receive(&hspi1, &value, 1, 1000);
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_SET);
	return value;
}

void Epaper_Send(uint8_t reg, uint8_t *data, uint8_t length) {
	HAL_Delay(1);
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_RESET);
	_Epaper_Transmit_Byte(0x70); // Register Index
	_Epaper_Transmit_Byte(reg);

	_Epaper_Blink_CS();

	_Epaper_Transmit_Byte(0x72); // Register Write
	for (int i = 0; i < length; i++) {
		_Epaper_Transmit_Byte(data[i]);
	}
	HAL_GPIO_WritePin(EPAPER_CS_GPIO_Port, EPAPER_CS_Pin, GPIO_PIN_SET);
}

void Epaper_Send_Byte(uint8_t reg, uint8_t data) {
	Epaper_Send(reg, &data, 1);
}

void Epaper_Write_Raw_Line(uint8_t y, uint8_t *line) {
	for (int i = 0; i < EPAPER_LINE_BUFFER_SIZE; i++) {
		epaperLineBuffer.uint8[i] = 0;
	}

	for (int i = 0; i < 33; ++i) {
		epaperLineBuffer.data.odd[i] = 0xAA
				| line[(65 - i) * 4 - 7]
				| (line[(65 - i) * 4 - 5] << 2)
				| (line[(65 - i) * 4 - 3] << 4)
				| (line[(65 - i) * 4 - 1] << 6);
		epaperLineBuffer.data.even[i] = 0xAA
				| line[i * 4]
				| (line[i * 4 + 2] << 2)
				| (line[i * 4 + 4] << 4)
				| (line[i * 4 + 6] << 6);
	}
	epaperLineBuffer.data.scan[(175 - y) / 4] = 0x03 << ((y % 4) * 2);

	Epaper_Send(0x0A, epaperLineBuffer.uint8, EPAPER_LINE_BUFFER_SIZE);
	// Turn on OE (output data to display)
	Epaper_Send_Byte(0x02, 0x07);
}

void Epaper_Write_Raw_8Lines(uint8_t y, uint8_t *line) {
	uint8_t lineBuf[EPAPER_COLS];
	for (int i = 0; i < 8; ++i) {
		for (int j = 0; j < EPAPER_COLS; ++j) {
			lineBuf[EPAPER_COLS - 1 - j] = (line[j] & (0x1 << i)) != 0;
		}
		Epaper_Write_Raw_Line(y * 8 + i, lineBuf);
	}
}

void Epaper_Write_StrLine(uint8_t y, char *msg) {
	uint8_t *lineBuf = epaperScreenBuffer[2];
	uint16_t pos = 0;

	for (int i = 0; i < EPAPER_COLS; ++i) {
		lineBuf[i] = 0;
	}

	while (*msg != '\0') {
		uint8_t *fontChar = font[(uint8_t)(*msg)];
		if (fontChar == NULL) {
			return;
		}
		uint8_t size = *fontChar;
		uint8_t kerning = 1;

		if (pos + size + kerning >= EPAPER_COLS) {
			pos = 0;
		}

		for (int i = 1; i <= size; i++) {
			lineBuf[pos++] = fontChar[i];
		}
		pos += kerning;
		++msg;
	}
	//Epaper_Write_Raw_8Lines(y, lineBuf);
	Epaper_Flush();
}

void Epaper_Flush() {
	uint8_t lineBuf[EPAPER_COLS];


	for (int round = 0; round < 2; ++round) {
		for (int y = 0; y < EPAPER_8LINES; ++y) {
			for (int i = 0; i < EPAPER_COLS; ++i) {
				lineBuf[i] = round % 2 ? 0xFF : 0x00;
			}
			Epaper_Write_Raw_8Lines(y, lineBuf);
		}
	}

	for (int y = 0; y < EPAPER_8LINES; ++y) {
		for (int i = 0; i < EPAPER_COLS; ++i) {
			lineBuf[i] = ~epaperScreenBuffer[y][i];
		}
		Epaper_Write_Raw_8Lines(y, lineBuf);
	}

	for (int round = 0; round < 5; ++round) {
		for (int y = 0; y < EPAPER_8LINES; ++y) {
			for (int i = 0; i < EPAPER_COLS; ++i) {
				lineBuf[i] = epaperScreenBuffer[y][i];
			}
			Epaper_Write_Raw_8Lines(y, lineBuf);
		}
	}
}

void Epaper_Clear() {
	for (int y = 0; y < EPAPER_8LINES; ++y) {
		for (int i = 0; i < EPAPER_COLS; ++i) {
			epaperScreenBuffer[y][i] = 0;
		}
	}
	Epaper_Flush();
}
