
// event representation
class Event implements Comparable {

 public Event(int a_type, double a_time, int a_sequenceNum) { _type = a_type; time = a_time; sequenceNum = a_sequenceNum;}
  
 public double time;
 private int _type;
 public int sequenceNum;

// public double dtime;

 //public int get_departtime() { return dtime; }

 public int get_sequenceNum() { return sequenceNum; }
 
 public int get_type() { return _type; }
 public double get_time() { return time; }


 public Event leftlink, rightlink, uplink;

 public int compareTo(Object _cmpEvent ) {
  double _cmp_time = ((Event) _cmpEvent).get_time() ;
  if( this.time < _cmp_time) return -1;
  if( this.time == _cmp_time) return 0;
  return 1;
 }
};
