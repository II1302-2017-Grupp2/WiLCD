#include <stdint.h>
#include <string.h>
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
void Epaper_Demo();
void Epaper_Draw_HorizLine(int y, uint8_t yPos);
void Epaper_Write_Raw_Line(int y, uint8_t *line, uint8_t invert);
void Epaper_Write_Raw_8Lines(int y, uint8_t *line, uint8_t invert);
void Epaper_Write_StrLine(int y, char *msg);
void Epaper_Write_StrnLine(int y, uint8_t *msg, int len);
void Epaper_Write_Nothing_Frame();
void Epaper_Write_Dummy_Line();

void Epaper_Splash_Init();
void Epaper_Splash_Shutdown();
void Epaper_Splash_Status(char *msg);

void Epaper_MessageCard_Display(uint8_t *msg, int16_t len);
void Epaper_MessageCard_Update();

#endif
