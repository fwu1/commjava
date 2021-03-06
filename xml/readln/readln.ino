#include <Stepper.h>

const int stepsPerRevolution = 64;  // change this to fit the number of steps per revolution

Stepper stepper1(stepsPerRevolution, 10, 12, 11, 13);

void setup() {
  Serial.begin(9600);
  // set the speed at 60 rpm:
  //stepper1= new Stepper(stepsPerRevolution, 10, 12, 11, 13);
  stepper1.setSpeed(240);
  
}


#define BUFFSIZE 100
char buff[BUFFSIZE+1];
int buffCount=0;
int count=0;
//int wordPos=0;
int wordEndPos=0;

#define keyRETRUN		'\r'
#define keyBACKSPACE  	127
#define NoPin			0xFF
int error=0;

char * getWord()
{
	int wordPos=-1;
	while(wordEndPos<buffCount && buff[wordEndPos]==' ')
		wordEndPos++;
	if(wordEndPos<buffCount && buff[wordEndPos]!=' ')
		wordPos=wordEndPos;
	else
		return NULL;

	while(wordEndPos<buffCount && (buff[wordEndPos]!=' ' && buff[wordEndPos]!=0))
		wordEndPos++;
	if(wordEndPos>wordPos) {
		buff[wordEndPos++]=0;
		return buff+wordPos;
	}
	return NULL;
}

uint8_t getPinID(char * pinName)
{
	uint8_t id=NoPin;
	char c0=pinName[0];
	char c1=pinName[1];
	if(c0=='a' && c1>='0' && c1<='7')  
		return A0+(c1-'0');
	if(c0>='0' && c0<='9') {
		uint8_t id0=c0-'0';
		if(c1>='0' && c1<='9') {
			id=10*id0+(c1-'0');
		}
		else if (c1==0)
			id=id0;
	}
	return id;
}

int parseInt(char* txt)
{
	error=1;
	if(txt==NULL)
		return 0;
	int sign=1;
	if(*txt=='-') {
		sign=-1;
		txt++;
	}
	int v=0;
	while(*txt>='0' && *txt<'9') {
		v=10*v+(*txt-'0');
		txt++;
	}
	if(*txt==0) {
		error=0;
		return sign*v;
	}
	return 0;
}

void process()
{
	char *rst="Fail";
	buff[buffCount]=0;
	
	char txt[200];
	//sprintf(txt,"P:'%s'\r\n",buff);
	//Serial.write(txt);
	
	char *word;
	wordEndPos=0;
	
	char * cmd=getWord();
	//Serial.println(cmd);
	
	toLowcase(cmd);
	if(strcmp(cmd,"pinmode")==0 || strcmp(cmd,"pm")==0) {
		char * pin =getWord();
		if(pin==NULL) 
			rst="No pin defined";
		else {
			toLowcase(pin);
			uint8_t pinId=getPinID(pin);
			if(pinId!=NoPin) {
				char * valueWord =getWord();
				if(valueWord==NULL) 
					rst="No value defined";
				else {
					toLowcase(valueWord);
					uint8_t value;
					int pass=1;
					if(strcmp(valueWord,"output")==0 || strcmp(valueWord,"o")==0)
						value=OUTPUT;
					else if(strcmp(valueWord,"input")==0 || strcmp(valueWord,"i")==0)
						value=INPUT;
					else {
						pass=0;
						rst="Wrong value";
					}
					
					if(pass) {
						pinMode(pinId,value);
						rst="OK";
					}
				}
			}
			else 
				rst="wrong pin name";
		}
			
	}
	else if(strcmp(cmd,"digitalWrite")==0 || strcmp(cmd,"dw")==0) {
		char * pin =getWord();
		if(pin==NULL) 
			rst="No pin defined";
		else {
			toLowcase(pin);
			uint8_t pinId=getPinID(pin);
			if(pinId!=NoPin) {
				char * valueWord =getWord();
				if(valueWord==NULL) 
					rst="No value defined";
				else {
					toLowcase(valueWord);
					uint8_t value;
					int pass=1;
					if(strcmp(valueWord,"high")==0 || strcmp(valueWord,"h")==0)
						value=HIGH;
					else if(strcmp(valueWord,"low")==0 || strcmp(valueWord,"l")==0)
						value=LOW;
					else {
						pass=0;
						rst="Wrong value";
					}
					
					if(pass) {
						digitalWrite(pinId,value);
						rst="OK";
					}
				}
			}
			else 
				rst="wrong pin name";
		}
			
	}	
	else if(strcmp(cmd,"analogRead")==0 || strcmp(cmd,"ar")==0) {
		char * pin =getWord();
		if(pin==NULL) 
			rst="No pin defined";
		else {
			toLowcase(pin);
			uint8_t pinId=getPinID(pin);
			if(pinId!=NoPin) {
				int value=analogRead(pinId);
				sprintf(txt,"%d OK",value);
				//sprintf(txt,"PASS",value);
				rst=txt;
			}
			else 
				rst="wrong pin name";
		}
			
	}
	else if(strcmp(cmd,"stepper")==0 || strcmp(cmd,"st")==0) {
		char * action =getWord();
		if(action==NULL) 
			rst="No action defined";
		else {
			if(strcmp(action,"move")==0 || strcmp(action,"m")==0) {
				// move action
				char * szSteps =getWord();
				int steps=parseInt(szSteps);
				if(error==0) {
					stepper1.step(steps);
					rst="OK";
				}
				else
					rst="wrong move step value";
			}
			else if(strcmp(action,"speed")==0 || strcmp(action,"sp")==0) {
				// set speed
				char * szSpeed =getWord();
				int speed=parseInt(szSpeed);
				if(error==0) {
					stepper1.setSpeed(speed);
					rst="OK";
				}
				else
					rst="Wrong speed value";
			}
			else
				rst="Wrong action name";
		}
	}
	
	else {
		rst="wrong command";
	}
	
	Serial.write(rst);
	Serial.write("\r\n");
	
/*	
	do {
		word=getWord();
		if(word!=NULL)
			Serial.println(word);
	} while (word!=NULL);
*/	
	
}

void toLowcase(char* word)
{
	while(*word!=0) {
		if(*word>='A' && *word<='Z')
			*word-=32;
		word++;
	}
}

// the loop routine runs over and over again forever:
void loop() {
  if (Serial.available()) {
    /* read the most recent byte */
    byte byteRead = Serial.read();
	
	// echo
	Serial.write(byteRead);
	if(byteRead==keyRETRUN) {
		Serial.write('\n');
		process();
		buffCount=0;
	}
	else if(byteRead==keyBACKSPACE) {
		// remove one character from the buffer
		if(buffCount>0)
			buffCount--;
	}
	else {
		if(byteRead<32) {
			//int d=byteRead;
			//Serial.write('#');
			//Serial.write('A'+byteRead);
		}
		else {
			// save to buff
			if(buffCount<BUFFSIZE) {
				buff[buffCount++]=byteRead;
		}
		}
	}
  }
}
