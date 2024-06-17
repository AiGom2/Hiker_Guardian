#include <Servo.h>
#include <SoftwareSerial.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
LiquidCrystal_I2C lcd(0x27, 16, 2);

#define DEBUG
#define ARR_CNT 5
#define CMD_SIZE 60
char sendBuf[CMD_SIZE];
char recvId[10] = "LMG_AND";
char lcdLine1[17] = "Smart Wild alarm";
char lcdLine2[17] = "Not Founded";
char lcdLine3[17] = "DANGER! KEEP OUT";
char lcdLine4[17] = "Wildanimal found";

unsigned long buzzerStartTime = 0;
unsigned long servoStartTime = 0;
unsigned long servoInterval = 2000; // 서보 모터가 움직이는 간격 (2초)
unsigned long lastServoMoveTime = 0; // 마지막 서보 모터 움직인 시간
unsigned long warningStartTime = 0;
unsigned int secCount;
int angle = 90;
int servoDirection = 1; // 서보 모터의 방향 (1: 정방향, -1: 역방향)

SoftwareSerial BTSerial(10, 11); // RX ==>BT:TXD, TX ==> BT:RXD
Servo myservo1;
Servo myservo2;

void setup()
{
#ifdef DEBUG
  Serial.begin(115200);
  Serial.println("setup() start!");
#endif
  lcd.init();
  lcd.backlight();
  lcdDisplay(0, 0, lcdLine1);
  lcdDisplay(0, 1, lcdLine2);
  pinMode(3, OUTPUT);
  BTSerial.begin(9600); 
  myservo1.attach(6);
  myservo2.attach(9);
  myservo1.write(0);
  myservo2.write(0);
}

void loop()
{
  if (BTSerial.available())
    bluetoothEvent();

#ifdef DEBUG
  if (Serial.available())
    BTSerial.write(Serial.read());
#endif

  // 서보 모터 제어
  if (servoStartTime != 0 && millis() - servoStartTime >= servoInterval) {
    if (millis() - lastServoMoveTime >= servoInterval) {
      // 2초마다 서보 모터 각도 변경
      myservo1.write(angle + 90 * servoDirection);
      myservo2.write(angle + 90 * servoDirection);
      servoDirection *= -1; // 방향 전환
      lastServoMoveTime = millis();
    }
  }

  // 20초가 지나면 서보 모터 멈춤
  if (servoStartTime != 0 && millis() - servoStartTime >= 40000) {
    noTone(3);
    myservo1.write(0);
    myservo2.write(0);
    servoStartTime = 0; // 서보 모터 시작 시간 초기화
  }
  if (warningStartTime != 0 && millis() - warningStartTime < 30000){
    for(int freq = 150; freq <=1800; freq += 2) 
      tone(3, freq);
    for(int freq = 1800; freq >=150; freq -= 2) 
      tone(3, freq);
  }
  // 야생동물 포착후 10분 동안 주의 문구 표시
  if (warningStartTime != 0 && millis() - warningStartTime >= 30000){
    lcdDisplay(0,0,lcdLine1);
    lcdDisplay(0,1,lcdLine2);
    warningStartTime = 0;
  }
  noTone(3);
}

void bluetoothEvent()
{
  int i = 0;
  char * pToken;
  char * pArray[ARR_CNT] = {0};
  char recvBuf[CMD_SIZE] = {0};
  int len = BTSerial.readBytesUntil('\n', recvBuf, sizeof(recvBuf) - 1);

#ifdef DEBUG
  Serial.print("Recv : ");
  Serial.println(recvBuf);
#endif

  pToken = strtok(recvBuf, "[@]");
  while (pToken != NULL)
  {
    pArray[i] =  pToken;
    if (++i >= ARR_CNT)
      break;
    pToken = strtok(NULL, "[@]");
  }
  
  if (!strcmp(pArray[1], "SERVO")) {
    sprintf(sendBuf, "[%s]%s\n", pArray[0], pArray[1]);
    lcdDisplay(0,0,lcdLine3);
    lcdDisplay(0,1,lcdLine4);
    buzzerStartTime = millis(); // 부저 시작 시간 기록
    servoStartTime = millis(); // 서보 모터 시작 시간 기록
    lastServoMoveTime = millis(); // 마지막 서보 모터 움직인 시간 초기화
    warningStartTime = millis(); // 경고문 시작 시간 기록
  }
  else if (!strncmp(pArray[1], " New", 4)) // New Connected
  {
    return ;
  }
  else if (!strncmp(pArray[1], " Alr", 4)) //Already logged
  {
    return ;
  }
  else{
    return ;
  }
#ifdef DEBUG
  Serial.print("Send : ");
  Serial.print(sendBuf);
#endif
  BTSerial.write(sendBuf);
}

void lcdDisplay(int x, int y, char * str) // 이전에 남아있던 LCD 문자 지우려는 함수
{
  int len = 16 - strlen(str);
  lcd.setCursor(x, y);
  lcd.print(str);
  for (int i = len; i > 0; i--)
    lcd.write(' ');
}
