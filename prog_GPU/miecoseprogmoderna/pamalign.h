#ifndef PAMSTUFF_H
#define PAMSTUFF_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

typedef unsigned int uint;
typedef unsigned short ushort;
typedef unsigned char uchar;

typedef struct imgInfo {
	uint width;
	uint height;
	uint channels;
	uint maxval;
	uint depth; /* bits per value */
	size_t data_size;
	void *data;
} imgInfo;

enum tupltype {
	TUPL_BLACKANDWHITE = 0,
	TUPL_GRAYSCALE = 1,
	TUPL_GRAYSCALE_ALPHA,
	TUPL_RGB,
	TUPL_RGB_ALPHA,
	TUPL_UNKNOWN
};

static const char *tuplname[TUPL_UNKNOWN] = {
	"BLACKANDWHITE",
	"GRAYSCALE",
	"GRAYSCALE_ALPHA",
	"RGB",
	"RGB_ALPHA"
};

static const char *PAM_HDR_WIDTH = "WIDTH";
static const char *PAM_HDR_HEIGHT = "HEIGHT";
static const char *PAM_HDR_DEPTH = "DEPTH";
static const char *PAM_HDR_MAXVAL = "MAXVAL";
static const char *PAM_HDR_TUPLTYPE = "TUPLTYPE";
static const char *PAM_HDR_END = "ENDHDR";

#define IS_NL(x) ((x) == 0x0A || (x) == 0x0D)
#define IS_SPACE(x) ((x) == 0x20 || (x) == 0x09 || IS_NL(x))
#define STREQ(a, b) (*(a) == *(b) && !strcmp(a, b))

static int read_pam_init(FILE *fp, imgInfo *img) {
	bool seenWidth = false;
	bool seenHeight = false;
	bool seenDepth = false;
	bool seenMaxval = false;
	bool seenEnd = false;

#define BUFLEN 256

	while (!seenEnd) {
		char buffer[BUFLEN];
		char label[BUFLEN+1];
		char value[BUFLEN+1];
		char *rc;
		rc = fgets(buffer, sizeof(buffer), fp);
		if (rc == NULL) {
			fprintf(stderr, "EOF or error reading file while trying to read the PAM header\n");
			return 1;
		}

		/* parse a header line */
		bool invalue = false;
		int cursor = 0;
		for (rc = buffer; rc < buffer + BUFLEN; ++rc) {
			if (invalue) {
				value[cursor++] = *rc;
				if (IS_NL(*rc)) {
					value[--cursor] = '\0';
					break;
				}
			} else {
				label[cursor++] = *rc;
				if (IS_SPACE(*rc)) {
					invalue = true;
					label[--cursor] = '\0';
					cursor = 0;
				}
				if (IS_NL(*rc))
					break;
			}
		}
		if (!invalue)
			continue; // not a header line we know about

		/* process a header line */
		if (STREQ(label, PAM_HDR_END)) {
			seenEnd = true;
		} else if (STREQ(label, PAM_HDR_WIDTH)) {
			seenWidth = true;
			img->width = atoi(value);
		} else if (STREQ(label, PAM_HDR_HEIGHT)) {
			seenHeight = true;
			img->height = atoi(value);
		} else if (STREQ(label, PAM_HDR_DEPTH)) {
			seenDepth = true;
			img->channels = atoi(value);
		} else if (STREQ(label, PAM_HDR_MAXVAL)) {
			seenMaxval = true;
			img->maxval = atoi(value);
			if (img->maxval <= 0xff)
				img->depth = 8;
			else if (img->maxval <= 0xffff)
				img->depth = 16;
			else {
				fprintf(stderr, "maxval too high\n");
				return 1;
			}
		} else if (STREQ(label, PAM_HDR_TUPLTYPE)) {
			/* TODO */
		}
	}
	if (!seenWidth || !seenHeight || !seenDepth || !seenMaxval) {
		fprintf(stderr, "incomplete header\n");
		return 1;
	}
	/* TODO check if we missed some important header */
	/* for the time being we assume we read a well-formed PAM file */
	return 0;
}

static const char *pam_hdr = "P7\n";

void read_sample(FILE *fd, const imgInfo *img, uint cur) {
	uchar *data8 = (uchar*)img->data;
	ushort *data16 = (ushort*)img->data;
	switch (img->depth) {
	case 8:
		data8[cur] = fgetc(fd);
		break;
	case 16:
		data16[cur] = (ushort)(fgetc(fd)) << 8 | fgetc(fd);
		break;
	default:
		fprintf(stderr, "this can't happen\n");
	}
}

void write_sample(FILE *fd, const imgInfo *img, const uint cur) {
	const uchar *data8 = (const uchar*)img->data;
	const ushort *data16 = (const ushort*)img->data;
	ushort datum;
	switch (img->depth) {
	case 8:
		fputc(data8[cur] & 0xff, fd);
		return;
	case 16:
		datum = data16[cur];
		fputc((datum >> 8) & 0xff, fd);
		fputc(datum & 0xff, fd);
		return;
	default:
		fprintf(stderr, "this can't happen\n");
	}
}

int load_pam(const char *fname, imgInfo *img) {
	FILE *fp = fopen(fname, "rb");
	if (!fp) {
		fprintf(stderr, "could not open %s\n", fname);
		return 1;
	}
	char hdr[4];
	fread(hdr, 3, 1, fp);
	hdr[3] = '\0';
	if (strcmp(hdr, pam_hdr)) {
		fprintf(stderr, "not a PAM file: %s\n", fname);
		fclose(fp);
		return 1;
	}
	read_pam_init(fp, img);
	if (img->channels < 1 || img->channels > 4) {
		fprintf(stderr, "can't process PAM file with %u channels\n", img->channels);
		fclose(fp);
		return 1;
	}
	size_t memSize = img->depth/8;
	memSize *= img->channels + (img->channels == 3); /* pad 3 to 4 channels */
	memSize *= img->width;
	memSize *= img->height;
	img->data_size = memSize;
	img->data = malloc(memSize);
	if (img->data == NULL) {
		fprintf(stderr, "can't allocate memory for image data\n");
		return 1;
	}

	uint cur = 0;
	for (uint row = 0; row < img->width; ++row) {
		for (uint col = 0; col < img->height; ++col) {
			for (uint ch = 0; ch < img->channels; ++ch) {
				read_sample(fp, img, cur++);
			}
			/* skip padding */
			if (img->channels == 3)
				++cur;
		}
	}
	fclose(fp);
	return 0;
}

int save_pam(const char *fname, const imgInfo *img) {
	FILE *fp = fopen(fname, "wb");
	if (!fp) {
		fprintf(stderr, "could not open %s for writing\n", fname);
		return 1;
	}
	fputs(pam_hdr, fp);
	fprintf(fp, "%s %u\n", PAM_HDR_WIDTH, img->width);
	fprintf(fp, "%s %u\n", PAM_HDR_HEIGHT, img->height);
	fprintf(fp, "%s %u\n", PAM_HDR_DEPTH, img->channels);
	fprintf(fp, "%s %u\n", PAM_HDR_MAXVAL, img->maxval);
	fprintf(fp, "%s %s\n", PAM_HDR_TUPLTYPE, tuplname[img->channels]);
	fprintf(fp, "%s\n", PAM_HDR_END);
	uint cur = 0;
	for (uint row = 0; row < img->width; ++row) {
		for (uint col = 0; col < img->height; ++col) {
			for (uint ch = 0; ch < img->channels; ++ch) {
				write_sample(fp, img, cur);
				++cur;
			}
			if (img->channels == 3)
				++cur;
		}
	}
	fclose(fp);
	return 0;
}

#endif
