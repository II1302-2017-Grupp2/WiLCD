#include <stdint.h>
#include "font.h"

#ifndef __epaper_H
#define __epaper_H

void Epaper_Init();
void Epaper_Shutdown();
uint8_t Epaper_Read(uint8_t reg, uint8_t data);
uint8_t Epaper_Read_ID();
void Epaper_Send(uint8_t reg, uint8_t *data, uint8_t length);
void Epaper_Send_Byte(uint8_t reg, uint8_t data);

void Epaper_Clear();
void Epaper_Flush();
void Epaper_Write_Raw_Line(uint8_t y, uint8_t *line);
void Epaper_Write_Raw_8Lines(uint8_t y, uint8_t *line);
void Epaper_Write_StrLine(uint8_t y, char *msg);

#endif