package missile;
import mytools.*;
import vgpackage.*;
public final class Angle2
    implements VidGameGlobals {
  private static int sinTable[];

  private int r;

  static {
    constructSinTable();
  }

  // Calculate the arcSin of a value.
  // Precondition:
  //	val = double, the value to test
  // Postcondition:
  //	returns false if value was out of range (|val| >= 1.0),
  //  else returns true and has set angle to the arcSine of the value.
  public boolean arcSine(double val) {
    if (val <= -1.0 || val >= 1.0)
      return false;
    set( (int) (Math.asin(val) * RADTODEG));
    return true;
  }

  public boolean equals(Angle2 compare) {
    return r == compare.r;
  }

  public Angle2() {
    this(0);
  }

  public static int sin(Angle2 a) {
    return sinTable[a.getInt()];
  }

  public static int cos(Angle2 a) {
    int r = ( (a.getInt()) + 64) & 0xff;
    return sinTable[r];
  }

  public static int scaleUp(int val) {
    return val << TRIGBITS;
  }

  public static void createRay(int magnitude, Angle2 direction, Pt p) {
    final Pt zero = new Pt();
    createRay(zero, magnitude, direction, p);
    //p.x = (cos(direction) * magnitude) >> Angle.TRIGBITS;
    //p.y = (sin(direction) * magnitude) >> Angle.TRIGBITS;
  }

  /**
     Calculate the location of a point a certain distance
     and angle from a starting point

     @param origin : starting point
     @param magnitude : distance from origin
     @param direction : rotation around point
     @param p : where to store calculated point
   */
  public static void createRay(Pt origin, int magnitude, Angle2 direction, Pt p) {
    p.x = origin.x + ((cos(direction) * magnitude) >> TRIGBITS);
    p.y = origin.y + ((sin(direction) * magnitude) >> TRIGBITS);
  }

/*  // Calculate a ray in a particular direction
  // Precondition:
  //	magnitude = magnitude of ray
  //	direction = direction of rotation from origin
  // Postcondition:
  //	ray endpoint returned
  public static Pt createRay(int magnitude, Angle direction) {
    Pt p = new Pt();
    createRay(magnitude, direction, p);
    return p;
  }
*/

  private static void constructSinTable() {
    sinTable = new int[256];
    for (int i = 0; i < 256; i++) {
      sinTable[i] = (int) (Math.floor(Math.sin( (i * 2 * Math.PI) / 256.0) *
                                      (1 << Angle2.TRIGBITS)));
    }
  }

  public Angle2(int n) {
    this(n, false);
  }

  public Angle2(int n, boolean intFlag) {
    if (intFlag)
      setInt(n);
    else
      set(n);
  }

  public Angle2(int x, int y) {
    calc(x, y);
  }

  public void calc(int x, int y) {
    if ( (x | y) == 0)
      set(0);
    else {
      double val = Math.atan2(y, x);
      set( (int) (Math.atan2(y, x) * RADTODEG));
    }
  }

  public void set(int r) {
    this.r = (r & (MAX - 1));
  }

  public void random() {
    r = MyMath.rnd(MAX);
  }

  public int get() {
    return r;
  }

  public int getInt() {
    return (r >> TRIGBITS) & 0xff;
  }

  public void setInt(int r) {
    set(r << TRIGBITS);
  }

  public void copyTo(Angle2 dest) {
    dest.r = r;
  }

  public void adjust(int add) {
    r = (r + add) & (MAX - 1);
  }

  public void adjustInt(int add) {
    r = (r + (add << TRIGBITS)) & (MAX - 1);
  }

  // Calculate the distance of a point from a ray
  // Precondition:
  //	point's location, relative to origin of ray, in px,py
  //	ray endpoint in rx,ry
  // Postcondition:
  //	returns the distance between the point and the closest point on the ray
  public static int calcPtDistFromRay(int px, int py, int rx, int ry) {

    // Calculate the dot product
    int dotPS = px * rx + py * ry;

    if (dotPS <= 0) {
      return Pt.magnitude(px, py);
    }

    int sSquared = rx * rx + ry * ry;

    if (dotPS >= sSquared)
      return Pt.magnitude(px - rx, py - ry);

    int crossPS = Math.abs(px * ry - py * rx);

    int sRoot = (int) Math.sqrt(sSquared);

    return crossPS / sRoot;
  }

  // Make angle approach a desired value by a maximum velocity
  public void approach(Angle2 desired, int speed) {
    approach(desired.r, speed);
  }

  public void approach(int desired, int speed) {
    int diff = desired - r;
    if (diff <= - (MAX >> 1))
      diff += MAX;
    else if (diff >= (MAX >> 1))
      diff -= MAX;

    if (diff < 0) {
      speed = -speed;
      if (speed < diff)
        speed = diff;
    }
    else
    if (speed > diff)
      speed = diff;
    r += speed;
  }


}