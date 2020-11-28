#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "network.h"
#include "error_messages.h"

void		usage(const char prog_name[const])
{
  fprintf(stderr, ERROR_DISPLAY_USAGE, prog_name);
}

uint8_t         *get_hardware_address(const char * const hardware_addr)
{
  uint8_t       *mac;

  if (!(mac = malloc(sizeof(uint8_t) * HARDWARE_LENGTH)))
    return (NULL);
  memcpy(mac, hardware_addr, sizeof(uint8_t) * HARDWARE_LENGTH);
  return (mac);
}
