#include <stdint.h>

#ifndef __utils_H
#define __utils_H

char bcdToChar(uint8_t bcd);
char *dateTimeStr();
void setDateTime(uint8_t *str, uint16_t len);
int16_t wordSize(uint8_t *str, int16_t len);

#endif
