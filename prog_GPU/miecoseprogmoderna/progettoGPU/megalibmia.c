#include "CL/cl.h"
#include <unistd.h>
#include <stdio.h>
#include <errno.h>

void checkError(int err){
	if(err != CL_SUCCESS){
		char str[4];
		sprintf(str,"%i",err);
		execlp("grep", "grep","--", str,"/usr/include/CL/cl.h", (char*)NULL);
		perror("execfalsogrep");
		exit(1);

	}
}

void DisplayPlatformInfo( cl_platform_id id, cl_platform_info name, const char *  str){
	cl_int errNum;
	size_t paramValueSize;
	errNum = clGetPlatformInfo( id, name, 0, NULL, &paramValueSize);
	checkError(errNum);
	char * info =malloc(sizeof(char) * paramValueSize);
	errNum = clGetPlatformInfo(
	id,
	name,
	paramValueSize,
	info,
	NULL);
	checkError(errNum);
	printf("\t %s :\t %s \n ",str,info);
}
void displayInfo(void){
	cl_int errNum;
	cl_uint numPlatforms;
	cl_platform_id * platformIds;
	cl_context context = NULL;
	// First, query the total number of platforms
	errNum = clGetPlatformIDs(0, NULL, &numPlatforms);
	if (errNum != CL_SUCCESS || numPlatforms <= 0)
	{
		printf("Failed to find any OpenCL platform.");
		return;
	}
	// Next, allocate memory for the installed platforms, and query
	// to get the list.
	platformIds = (cl_platform_id * )malloc(
	sizeof(cl_platform_id) * numPlatforms);
	// First, query the total number of platforms
	errNum = clGetPlatformIDs(numPlatforms, platformIds, NULL);
	checkError(errNum);	
	printf("Number of platforms: \t");
	// Iterate through the list of platforms displaying associated
	// information
	for (cl_uint i = 0; i < numPlatforms; i++) {
	// First we display information associated with the platform
	DisplayPlatformInfo(
	platformIds[i], CL_PLATFORM_PROFILE, "CL_PLATFORM_PROFILE");
	DisplayPlatformInfo(
	platformIds[i], CL_PLATFORM_VERSION, "CL_PLATFORM_VERSION");
	DisplayPlatformInfo(
	platformIds[i], CL_PLATFORM_VENDOR, "CL_PLATFORM_VENDOR");
	DisplayPlatformInfo(
	platformIds[i],
	CL_PLATFORM_EXTENSIONS,
	"CL_PLATFORM_EXTENSIONS");
	}
}
