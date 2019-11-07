#include "CL/cl.h"
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include "megalibmia.c"

int main(){
	checkError(-50);
	displayInfo();
	return 0;
}

