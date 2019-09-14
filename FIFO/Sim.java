import java.util.*;

class Sim {
	// Class Sim variables
	public static double Clock, MeanInterArrivalTime, MeanServiceTime, SIGMA, LastEventTime,
        TotalBusy, MaxQueueLength, SumResponseTime, curr, inter;
	public static long  NumberOfCustomers, QueueLength, NumberInService,
        TotalCustomers, NumberOfDepartures, LongService;

	public final static int arrival = 1;
	public final static int departure = 2;

	public static EventList FutureEventList;
	public static Queue Customers;
	public static Random stream;

	public static Event noWait;

	public static ArrayList<Integer> numbers;
	
	public static double x, y, endtoenddelays, delay, interarrtime, servicetimeR;
	public static int TotalPackets;
	public static int maxSizeQ;

	public static int iter;
	public static int count;
	
	public static int outorderCount;
	public static int loss;
	public static int currentSeq;
	
	public static double servertransSpeed = 0.0008;
	public static double routertransSpeed = 0.0008;
	
	public static int busy = 0;
	public static int idle = 0;
	public static int lc = 0;
	public static int hc = 0;

	public static void main(String argv[]) {
		MeanInterArrivalTime = 1/1125;
  		long seed            = Long.parseLong(argv[0]);

  		stream = new Random(seed);           // initialize rng stream
 		FutureEventList = new EventList();
  		Customers = new Queue();
		TotalPackets  = 100000;

 	 	x=3.1;
  		y=5.5;
 
  		Initialization();

  		// Loop until first "TotalCustomers" have departed
  		while(NumberOfDepartures < TotalPackets ) {
    		Event evt = (Event)FutureEventList.getMin();  // get imminent event
    		FutureEventList.dequeue();                    // be rid of it
    		Clock = evt.get_time();                       // advance simulation time
    		if( evt.get_type() == arrival ) {
    			ProcessArrival(evt);
    		}
    		else {
    			ProcessDeparture(evt);
    		}
    		//System.out.println("********************************************");
    	}
  		ReportGeneration();
 }

 // seed the event list with TotalCustomers arrivals
 	public static void Initialization()   { 
  		Clock = 0.0;
  		QueueLength = 0;
  		NumberInService = 0;
  		LastEventTime = 0.0;
  		TotalBusy = 0 ;
  		MaxQueueLength = 0;
  		SumResponseTime = 0;
  		NumberOfDepartures = 0;
  		LongService = 0;

  		maxSizeQ = 10000;
  		iter = 1;
  		count = 0;

 		numbers = new ArrayList<Integer>(TotalPackets);
		for(int i = 0; i < TotalPackets; i++) {
			numbers.add(i);
		}

		curr = 0.0;
		Collections.shuffle(numbers);

		endtoenddelays = 0.0;
  		outorderCount = 0;
  		loss = 0;
  		currentSeq = numbers.get(0);

  		while (( delay = normal(stream, x, y)) < 0 );
		interarrtime = servertransSpeed+delay+exponential( stream, MeanInterArrivalTime);
		endtoenddelays = endtoenddelays+ servertransSpeed+delay;

			//System.out.println("The inter arrival time: "+interarrtime);
			//System.out.println("The first normal delay is: \t"+delay);

			//System.out.println("ejriejriejrei \t"+endtoenddelays);
  // create first arrival event
  Event evt = new Event(arrival, interarrtime, currentSeq);
  //System.out.println("The first packet seq is: \t"+evt.get_sequenceNum());
  FutureEventList.enqueue( evt );
  numbers.remove(0);
 }

 public static void ProcessArrival(Event evt) {
  // if the server is idle, fetch the event, do statistics
  // and put into service
  if( NumberInService == 0) {
  		if(evt.get_sequenceNum() > currentSeq) {
					//System.out.println("This packet's seq is "+evt.get_sequenceNum()+" compared to "+currentSeq+" is Larger IDEL");
				currentSeq = evt.get_sequenceNum();
					//System.out.println("CurrentSeq is changed to "+currentSeq);
 		}
 		else{
				if(evt.get_sequenceNum() != currentSeq) {
						//System.out.println("This packet's seq is "+evt.get_sequenceNum()+" compared to "+currentSeq+" is Smaller IDEL");
					outorderCount++; 
						//System.out.println("First OC");
				}
 			}
			noWait = evt;	
				//System.out.println("The seq for this nowait packet is IDEL "+noWait.get_sequenceNum());
		
		ScheduleDeparture(3);

		idle++;
  }
  else {
  	if(QueueLength < maxSizeQ) {
  			Customers.enqueue(evt); 
  			QueueLength++;
  	}else{
  		loss++;
  	}

  	if(evt.get_sequenceNum() > currentSeq) {
  		//System.out.println("This packet's seq is "+evt.get_sequenceNum()+" compared to "+currentSeq);
  		currentSeq = evt.get_sequenceNum();
  		//System.out.println("CurrentSeq is changed to "+currentSeq);
  	}else{
  		outorderCount++;
  	}

	TotalBusy += (Clock - LastEventTime); 
  	busy++;
  	 // server is busy
  }
 

	if(iter < TotalPackets) {
			while (( delay = normal(stream, x, y)) < 0 );
			interarrtime = Clock+servertransSpeed+delay+exponential( stream, MeanInterArrivalTime);

				//System.out.println("The "+iter+ "th normal delay is: \t"+delay);
				//System.out.println("The "+iter+ "th inter arrival time is: \t"+interarrtime);

				endtoenddelays = endtoenddelays+delay+servertransSpeed;


			
			Collections.shuffle(numbers);
			
			Event next_arrival = new Event(arrival, interarrtime, numbers.get(0));
			numbers.remove(0);
				//System.out.println("The "+iter+ "th packet seq is: \t"+next_arrival.get_sequenceNum());		
				
			FutureEventList.enqueue( next_arrival );
			LastEventTime = Clock;
			
			iter++;
	}
 }

 public static void ScheduleDeparture(int i) {
 	if (i !=3) {
 		 servicetimeR = Clock+routertransSpeed+0.05;
 		 endtoenddelays = endtoenddelays+routertransSpeed+0.05;
  	Event depart = new Event(departure, servicetimeR, 8);
  	FutureEventList.enqueue( depart );
  	NumberInService = 1;
 	 QueueLength--;
  		//System.out.println("QUEUE is departing packets");
	//System.out.println("Service time is "+servicetimeR);
 	}
 	else{
 		servicetimeR = Clock+routertransSpeed+0.05;
 			endtoenddelays = endtoenddelays+routertransSpeed+0.05;
			Event depart = new Event(departure, servicetimeR, 3);
			FutureEventList.enqueue( depart );
			NumberInService = 1;
			//System.out.println("Nowait is departing packets");
			//System.out.println("Service time is "+servicetimeR);
 	}
 }

public static void ProcessDeparture(Event e) {
	Event finished;
	if(e.get_sequenceNum() != 3) {
		// get the customer description
 		finished = (Event) Customers.dequeue();
 // if there are customers in the queue then schedule
 // the departure of the next one
  		//System.out.println("QUEUE has sent a packet");
			//System.out.println("***************************");
	}
	else{
		finished = noWait;
					//System.out.println("No wait has sent a packet");
			//System.out.println("***************************");
			//System.out.println("This is should be 3:     " + e.get_sequenceNum());
	}

 if( QueueLength > 0 ) ScheduleDeparture(1);
  else NumberInService = 0;

  // measure the response time and add to the sum
  double response = (Clock - finished.get_time());
  SumResponseTime += response;
  if( response > 4.0 ) LongService++; // record long service
  TotalBusy += (Clock - LastEventTime );
  NumberOfDepartures++;
  LastEventTime = Clock;

  //System.out.println("Customer "+ count);  

  //System.out.println("Leaves at: " +LastEventTime);
 }

public static void ReportGeneration() {
double RHO   = TotalBusy/Clock;
double AVGR  = SumResponseTime/TotalPackets;
double PC4   = ((double)LongService)/TotalPackets;


System.out.println("\n");
System.out.println("\n");
System.out.println( "\tMEAN INTERARRIVAL TIME                         " 
	+ MeanInterArrivalTime );
System.out.println( "\tMEAN, NORMAL DELAY            			      " + x);
System.out.println( "\tSTANDARD DEVIATION OF SERVICE TIMES            " + y );
System.out.println( "\tNUMBER OF CUSTOMERS SERVED                     " + TotalPackets );
System.out.println(); 
System.out.println( "\tSERVER UTILIZATION                             " + RHO );
System.out.println( "\tAVERAGE RESPONSE TIME                          " + AVGR );
System.out.println( "\tPROPORTION WHO SPEND FOUR "); 
System.out.println( "\t MINUTES OR MORE IN SYSTEM                     " + PC4 );
System.out.println( "\tSIMULATION RUNLENGTH                           " + Clock );
System.out.println( "\tNUMBER OF DEPARTURES                           " + TotalPackets );


System.out.println( "\tNUMBER OF Out-of-Order packets                 " + outorderCount );
System.out.println( "\tNUMBER OF Loss packets                         " + loss );

System.out.println( "\tNUMBER OF busy                                 " + busy);
System.out.println( "\tNUMBER OF idle                                 " + idle);
System.out.println( "\tTotaldelay is:                                 " + endtoenddelays);

}

public static double exponential(Random rng, double mean) {
 return -mean*Math.log( rng.nextDouble() );
}

public static double SaveNormal;
public static int  NumNormals = 0;
public static final double  PI = 3.1415927 ;

public static double normal(Random rng, double mean, double sigma) {
        double ReturnNormal;
        // should we generate two normals?
        if(NumNormals == 0 ) {
          double r1 = rng.nextDouble();
          double r2 = rng.nextDouble();
          ReturnNormal = Math.sqrt(-2*Math.log(r1))*Math.cos(2*PI*r2);
          SaveNormal   = Math.sqrt(-2*Math.log(r1))*Math.sin(2*PI*r2);
          NumNormals = 1;
        } else {
          NumNormals = 0;
          ReturnNormal = SaveNormal;
        }
        return ReturnNormal*sigma + mean ;
 }
}

