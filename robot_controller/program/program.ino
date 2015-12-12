#include <Servo.h>

/* Joint
 * 
 * (pin_cw, pin_ccw) control direction of
 * torque.
 * 
 * (HIGH, LOW) = clock wise
 * (LOW, HIGH) = counter clock wise
 * (LOW, LOW) = static
 * 
 * pin_pwr controlls the torque 0-255, (low-high)
 * 
 * pin_enc is input from encoder to determine angle
 * pin_cur reads current (analog)
 * pin_vel reads velocity (analog)
 */
typedef struct {
  byte id;

  // ouptu
  int pin_cw;
  int pin_ccw;
  int pin_pwr;

  // input
  int pin_enc;
  uint8_t a_pin_cur;
  uint8_t a_pin_vel;
} JointPins;

JointPins shoulder_pins = {
  1, // id

  11, // cw
  10, // ccw
  5, // pwr

  3, // enc
  2, // cur
  3 // vel
};

JointPins elbow_pins = {
  2, // id

  13, // cw
  12, // ccw
  6, // pwr

  2, // enc
  0, // cur
  1 // vel
};

Servo ball_drop;

void initJoint(JointPins joint) {
  pinMode(joint.pin_cw, OUTPUT);
  pinMode(joint.pin_ccw, OUTPUT);
  pinMode(joint.pin_pwr, OUTPUT);
  pinMode(joint.pin_enc, INPUT);
}

void setDirection(JointPins joint, int dir) {
  switch (dir) {
    case 1:
      digitalWrite(joint.pin_cw, HIGH);
      digitalWrite(joint.pin_ccw, LOW);
      break;
    case 2:
      digitalWrite(joint.pin_cw, LOW);
      digitalWrite(joint.pin_ccw, HIGH);
      break;
    case 3:
      digitalWrite(joint.pin_cw, LOW);
      digitalWrite(joint.pin_ccw, LOW);
      break;
    case 4:
      digitalWrite(joint.pin_cw, HIGH);
      digitalWrite(joint.pin_ccw, HIGH);
      break;
  }
}

void setCurrent(JointPins joint, byte current) {
  analogWrite(joint.pin_pwr, current);
}

double readVelocity(JointPins joint) {
  double vel = analogRead(joint.a_pin_vel);
  return -4000 + vel * 1.25 * 8000.0 / 1024.0;
}

  int readCurrent(JointPins joint) {
  return analogRead(joint.a_pin_cur);
  // return -8 + cur * 1.25 * 16.0 / 1024.0;
}

volatile unsigned long elbow_start;
volatile unsigned long elbow_delta;
volatile unsigned long elbow_last;

volatile unsigned long shoulder_delta;
volatile unsigned long shoulder_start;
volatile unsigned long shoulder_last;


int readAngle(JointPins joint) {
  double duration;
  switch(joint.id) {
    case 1: duration = shoulder_delta; break;
    case 2: duration = elbow_delta; break;
  }
  
  if (duration > 3881) {
    duration = 3881;
  }
  else if (duration < 3) {
    duration = 3;
  }

  return (int)(0.09283 * duration - 0.2785);
}

void startElbowTimer() {
  elbow_start = micros();
  attachInterrupt(digitalPinToInterrupt(elbow_pins.pin_enc), stopElbowTimer, RISING);
}

void stopElbowTimer() {
  unsigned long delta = micros() - elbow_start;
  attachInterrupt(digitalPinToInterrupt(elbow_pins.pin_enc), startElbowTimer, FALLING);
  elbow_delta = (delta + elbow_last) / 2;
  elbow_last = delta;
}

void startShoulderTimer() {
  shoulder_start = micros();
  attachInterrupt(digitalPinToInterrupt(shoulder_pins.pin_enc), stopShoulderTimer, RISING);
}

void stopShoulderTimer() {
  unsigned long delta = micros() - shoulder_start;
  attachInterrupt(digitalPinToInterrupt(shoulder_pins.pin_enc), startShoulderTimer, FALLING);
  shoulder_delta = (delta + shoulder_last) / 2;
  shoulder_last = delta;
}

void setup() {
  Serial.begin(115200);

  initJoint(shoulder_pins);
  initJoint(elbow_pins);

  setDirection(shoulder_pins, 3);
  setCurrent(shoulder_pins, 0);

  setDirection(elbow_pins, 3);
  setCurrent(elbow_pins, 0);

  attachInterrupt(digitalPinToInterrupt(elbow_pins.pin_enc), startElbowTimer, FALLING);
  attachInterrupt(digitalPinToInterrupt(shoulder_pins.pin_enc), startShoulderTimer, FALLING);

  ball_drop.attach(7);
}

byte recv[6] = {0, 0, 0, 0};
byte out[12] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

int ball_target = 0;
int ball_pos = 0;

void loop() {
  ball_drop.write(ball_target);

  // 5 zeros
  Serial.write(0);
  Serial.write(0);
  Serial.write(0);
  Serial.write(0);
  Serial.write(0);

  unsigned long t = millis();
  byte* tb = (byte*)&t;
  out[0] = tb[0];
  out[1] = tb[1];
  out[2] = tb[2];
  out[3] = tb[3];

  out[4] = 1;

  // shoulder
  int integer = readAngle(shoulder_pins);
  tb = (byte*)&integer;
  out[5] = tb[0];
  out[6] = tb[1];
  
  integer = readCurrent(shoulder_pins);
  tb = (byte*)&integer;
  out[7] = tb[0];
  out[8] = tb[1];

  out[9] = 1;

  // elbow
  integer = readAngle(elbow_pins);
  tb = (byte*)&integer;
  out[10] = tb[0];
  out[11] = tb[1];

  integer = readCurrent(elbow_pins);
  tb = (byte*)&integer;
  out[12] = tb[0];
  out[13] = tb[1];
  
  Serial.write(out, 14);
  
  // read and apply inputs
  while (Serial.read() != 0) {
    if (millis() - t > 10000) {
      return;
    }
  }

  Serial.readBytes(recv, 7);

  // shoulder
  setDirection(shoulder_pins, recv[0]);
  if (recv[1] == 1) {
    setCurrent(shoulder_pins, 0);
  } else {
    setCurrent(shoulder_pins, recv[2]);
  }

  // elbow
  setDirection(elbow_pins, recv[3]);
  if (recv[4] == 1) {
    setCurrent(elbow_pins, 0);
  } else {
    setCurrent(elbow_pins, recv[5]);
  }
  
  if (recv[6] == 1) {
    ball_target = 90;
  } else {
    ball_target = 0;
  }
}
