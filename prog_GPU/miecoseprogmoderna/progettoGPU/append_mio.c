#include<stdio.h>
#include<string.h>



int main(const int argc, const char** argv){
	FILE *pFile;

	pFile=fopen(argv[1], "a");
	if(pFile==NULL) {
	    perror("Error opening file.");
	}
	else {	
		for(int i = 2;i<argc-1;i++){
			fprintf(pFile,"%s, ",argv[i]);
		}
		fprintf(pFile,"%s",argv[argc-1]);
		fprintf(pFile,"\n");
	}
	fclose(pFile);
}
