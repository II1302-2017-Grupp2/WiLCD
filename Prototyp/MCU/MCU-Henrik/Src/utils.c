#include "utils.h"
#include "rtc.h"
#include "font.h"

char yearPrefix[2] = { '0', '0' };
char dateTimeStrBuf[17];

char bcdToChar(uint8_t bcd) {
  return '0' + (bcd & 0x0F);
}

uint8_t charToBcd(uint8_t chr) {
  return (chr - '0') & 0x0F;
}

char *dateTimeStr() {
  RTC_TimeTypeDef time;
  RTC_DateTypeDef date;
  HAL_RTC_GetTime(&hrtc, &time, RTC_FORMAT_BCD);
  HAL_RTC_GetDate(&hrtc, &date, RTC_FORMAT_BCD);

  dateTimeStrBuf[0] = yearPrefix[0];
  dateTimeStrBuf[1] = yearPrefix[1];
  dateTimeStrBuf[2] = bcdToChar(date.Year >> 4);
  dateTimeStrBuf[3] = bcdToChar(date.Year);
  dateTimeStrBuf[4] = '-';
  dateTimeStrBuf[5] = bcdToChar(date.Month >> 4);
  dateTimeStrBuf[6] = bcdToChar(date.Month);
  dateTimeStrBuf[7] = '-';
  dateTimeStrBuf[8] = bcdToChar(date.Date >> 4);
  dateTimeStrBuf[9] = bcdToChar(date.Date);
  dateTimeStrBuf[10] = ' ';
  dateTimeStrBuf[11] = bcdToChar(time.Hours >> 4);
  dateTimeStrBuf[12] = bcdToChar(time.Hours);
  dateTimeStrBuf[13] = ':';
  dateTimeStrBuf[14] = bcdToChar(time.Minutes >> 4);
  dateTimeStrBuf[15] = bcdToChar(time.Minutes);
  dateTimeStrBuf[16] = '\0';
  return dateTimeStrBuf;
}

void setDateTime(uint8_t *str, uint16_t len) {
  if (len < 19) {
    return;
  }

  RTC_TimeTypeDef time;
  RTC_DateTypeDef date;
  HAL_RTC_GetTime(&hrtc, &time, RTC_FORMAT_BCD);
  HAL_RTC_GetDate(&hrtc, &date, RTC_FORMAT_BCD);

  yearPrefix[0] = str[0];
  yearPrefix[1] = str[1];

  date.Year = charToBcd(str[2]) << 4
      | charToBcd(str[3]);
  date.Month = charToBcd(str[5]) << 4
      | charToBcd(str[6]);
  date.Date = charToBcd(str[8]) << 4
      | charToBcd(str[9]);

  time.Hours = charToBcd(str[11]) << 4
      | charToBcd(str[12]);
  time.Minutes = charToBcd(str[14]) << 4
      | charToBcd(str[15]);
  time.Seconds = charToBcd(str[17]) << 4
      | charToBcd(str[18]);

  HAL_RTC_SetTime(&hrtc, &time, RTC_FORMAT_BCD);
  HAL_RTC_SetDate(&hrtc, &date, RTC_FORMAT_BCD);
}

int16_t wordSize(uint8_t *str, int16_t len) {
  int16_t total = 0;
  for (int i = 0; i < len && str[i] != ' '; ++i) {
    uint8_t *fontCharSize = font[str[i]];
    if (fontCharSize != NULL) {
      total += *fontCharSize + FONT_KERNING;
    }
  }
  return total;
}
