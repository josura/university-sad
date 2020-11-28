SRC	= src/main.c \
	src/utils/misc.c \
	src/network/interface.c \
	src/network/network.c \
	src/network/packet.c \

OBJ	= $(SRC:.c=.o)

NAME	= arp_poisoning

RM	= rm -f

CC	= gcc

CFLAGS	= -Wall -Wextra -Werror
CFLAGS	+= -pedantic-errors
CFLAGS	+= -I./include -g

all: $(NAME)

$(NAME): $(OBJ)
	$(CC) $(OBJ) -o $(NAME)

clean:
	$(RM) $(OBJ)

fclean: clean
	$(RM) $(NAME)

re: fclean all

.PHONY: all clean fclean re
