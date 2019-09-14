import java.util.*;


class Sim {
	// Class Sim variables
	public static double Clock, MeanInterArrivalTime, MeanServiceTime, SIGMA, LastEventTime, TotalBusy, MaxQueueLength, SumResponseTime;
	public static long  NumberOfCustomers, QueueLength, NumberInService, NumberOfDepartures, LongService;

	public final static int arrival = 1;
	public final static int departure = 2;

	public static EventList FutureEventList;
	
	public static Random stream;
	
	public static Queue highQ;
	public static Queue lowQ;
	
	public static Event noWait;
	
	public static ArrayList<Integer> numbers;
	
	public static double x, y, endtoenddelays, delay, interarrtime, servicetimeR;
	public static int TotalPackets;
	public static int maxSizeQ;
	public static int highQSize;
	public static int lowQSize;
	public static int iter;
	public static int count;
	public static int stat;
	
	public static double[] arr;
	
	
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
		TotalPackets  = 100000;
		
		x = 3.1; //Normal delay Mean
		y = 5.5; //Normal delay S.D.
		
		long seed = Long.parseLong(argv[0]);
		stream = new Random(seed);        // initialize rng stream
		FutureEventList = new EventList();

		highQ = new Queue();
		lowQ = new Queue();
		
		Initialization();

		// Loop until first "TotalCustomers" have departed
		while(NumberOfDepartures < TotalPackets) {
			Event evt = (Event)FutureEventList.getMin();			// get imminent event
			FutureEventList.dequeue();								// be rid of it
			Clock = evt.get_time();                       // advance simulation time
			if( evt.get_type() == arrival) {
				ProcessArrival(evt);
				
				//System.out.println("Packet:  "+evt.get_sequenceNum()+"Has arrived");
			}
			else{
				ProcessDeparture(evt);
				//System.out.println("Packet:  "+evt.get_sequenceNum()+"Has left");
			}
			System.out.println(NumberOfDepartures);
			
		}
		ReportGeneration();   
	}


	// seed the event list with TotalCustomers arrivals
	public static void Initialization() {
		Clock = 0.0;
  		QueueLength = 0;
  		NumberInService = 0;
  		LastEventTime = 0.0;
  		TotalBusy = 0 ;
  		MaxQueueLength = 0;
  		SumResponseTime = 0;
  		NumberOfDepartures = 0;
  		LongService = 0;
		
		arr = new double[TotalPackets];

  		maxSizeQ = 10000; //max queue size
		iter = 1;
		
		numbers = new ArrayList<Integer>(TotalPackets);
		for(int i = 0; i < TotalPackets; i++) {
			numbers.add(i);
		}
		//System.out.println(numbers);
		Collections.shuffle(numbers);
		//System.out.println(numbers);

  		highQSize = 0;
  		lowQSize = 0;
  		endtoenddelays = 0.0;
  		outorderCount = 0;
  		loss = 0;
		count = 0;
		currentSeq = numbers.get(0);
		
		
  		// create first arrival event
		while (( delay = normal(stream, x, y)) < 0 );
		interarrtime = servertransSpeed+delay+exponential( stream, MeanInterArrivalTime);
		
		endtoenddelays = endtoenddelays+servertransSpeed+delay;
		
			//System.out.println("The inter arrival time: "+interarrtime);
			//System.out.println("The first normal delay is: \t"+delay);
			
  		Event evt = new Event(arrival, interarrtime, currentSeq);
			//System.out.println("The first packet seq is: \t"+evt.get_sequenceNum());
  		FutureEventList.enqueue( evt );
		numbers.remove(0);
		//System.out.println("After removing the first one "+ numbers);
		
		arr[currentSeq] = interarrtime;
		//System.out.println("88888888888888888888888888888888888888888888888888888"+Arrays.toString(arr));
		
 	}

	public static void ProcessArrival(Event evt) {
		if(NumberInService == 0) { //Router is idle
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
			//transmits immediately
			noWait = evt;
				//System.out.println("The seq for this nowait packet is IDEL "+noWait.get_sequenceNum());
			ScheduleDeparture(3);
			idle++;
		}
		
		else{ //Router is busy
			if(evt.get_sequenceNum() > currentSeq) {
					//System.out.println("This packet's seq is "+evt.get_sequenceNum()+" compared to "+currentSeq+" is Larger BUSY");
				currentSeq = evt.get_sequenceNum();
					//System.out.println("CurrentSeq is changed to "+currentSeq);
				if(lowQSize < maxSizeQ) {
					lowQ.enqueue(evt);
  					lowQSize++;
					lc++;
				}else {
					loss++;
				}
  			}
			else {
  				if(highQSize < maxSizeQ) {
  					highQ.enqueue(evt);
  					highQSize++;
					hc++;
  				}
  				else {
  					loss++;
  				}
					//System.out.println("This packet's seq is "+evt.get_sequenceNum()+" compared to "+currentSeq+" is Smaller BUSY");
  				outorderCount++;
					//System.out.println("Second OC");
  			}
			TotalBusy += (Clock - LastEventTime);
			busy++;
		}
	
		// schedule the next arrival
		if(iter < TotalPackets) {
			while (( delay = normal(stream, x, y)) < 0 );
			interarrtime = Clock+servertransSpeed+delay+exponential( stream, MeanInterArrivalTime);
			endtoenddelays = endtoenddelays+servertransSpeed+delay;
			
				//System.out.println("The "+iter+ "th normal delay is: \t"+delay);
				//System.out.println("The "+iter+ "th inter arrival time is: \t"+interarrtime);
			
			Collections.shuffle(numbers);
			
			Event next_arrival = new Event(arrival, interarrtime, numbers.get(0));
			arr[numbers.get(0)] = interarrtime;
			
			//System.out.println("88888888888888888888888888888888888888888888888888888"+Arrays.toString(arr));
			numbers.remove(0);
				//System.out.println("The "+iter+ "th packet seq is: \t"+next_arrival.get_sequenceNum());		
				
			FutureEventList.enqueue( next_arrival );
			LastEventTime = Clock;
			
			iter++;
			//System.out.println(numbers);
		}
 	}

 	public static void ScheduleDeparture(int i) {
  		if(i == 1) { //HighQ
			servicetimeR = Clock+routertransSpeed+0.05;
			
			Event peek = (Event) highQ.peekFront();
			
  			Event depart = new Event(departure, servicetimeR, peek.get_sequenceNum());
  			FutureEventList.enqueue( depart );
  			NumberInService = 1;
  			highQSize--;
			//System.out.println("HighQ is departing packets");
			//System.out.println("Service time is "+servicetimeR);
			stat = 5;
  		}
  
  		if (i == 2) { //lowQ
			servicetimeR = Clock+routertransSpeed+0.05;
			Event peek = (Event) lowQ.peekFront();
			
  			Event depart = new Event(departure, servicetimeR, peek.get_sequenceNum());
  			FutureEventList.enqueue( depart );
  			NumberInService = 1;
  			lowQSize--;
			//System.out.println("LowQ is departing packets");
			//System.out.println("Service time is "+servicetimeR);
			stat = 4;
  		}
		
		if(i == 3) { //Nowait
			servicetimeR = Clock+routertransSpeed+0.05;
			
			endtoenddelays = endtoenddelays+routertransSpeed+0.05;
			
			Event depart = new Event(departure, servicetimeR, -1);
			FutureEventList.enqueue( depart );
			NumberInService = 1;
			//System.out.println("Nowait is departing packets");
			//System.out.println("Service time is "+servicetimeR);
			stat = 3;
		}
 	}

	public static void ProcessDeparture(Event e) {
		Event finished;
		
		if(stat == 5) { //highQ
			finished = (Event) highQ.dequeue();
			//System.out.println("HighQ has sent a packet");
			//System.out.println("***************************");
			//System.out.println("This is should be 5:     "+ e.get_sequenceNum());
		}
		
		else if(stat == 4){ //lowQ
			finished = (Event) lowQ.dequeue();
			//System.out.println("lowQ has sent a packet");
			//System.out.println("***************************");
			//System.out.println("This is should be 4:     " + e.get_sequenceNum());
		}
		else{
			finished = noWait;
			//System.out.println("No wait has sent a packet");
			//System.out.println("***************************");
			//System.out.println("This is should be 3:     " + e.get_sequenceNum());
		}
			
		if( highQSize+lowQSize == 0) {
			NumberInService = 0;
		}
		else{
			if(highQSize > 0) {
				ScheduleDeparture(1);
			}
			else {
				ScheduleDeparture(2);
			}
		}

  		// measure the response time and add to the sum
  		double response = (Clock - finished.get_time());
  		SumResponseTime += response;
  		if( response > 4.0 ) LongService++; // record long service
  		TotalBusy += (Clock - LastEventTime );
  		NumberOfDepartures++;
  		LastEventTime = Clock;
		
		if(e.get_sequenceNum()!= -1) {
			//System.out.println("\tPacket: "+e.get_sequenceNum()+"arrival time is"+arr[e.get_sequenceNum()]);
			//System.out.println("\tPacket: "+e.get_sequenceNum()+"left time is"+LastEventTime);
			
			endtoenddelays = endtoenddelays+(LastEventTime-arr[e.get_sequenceNum()]);
		}
 	}

	public static void ReportGeneration() {
		double RHO   = TotalBusy/Clock;
		double AVGR  = SumResponseTime/TotalPackets;
		double PC4   = ((double)LongService)/TotalPackets;


System.out.println("\n");
System.out.println("\n");
System.out.println( "\tMEAN INTERARRIVAL TIME FOR PACKETS             " 
	+ MeanInterArrivalTime );
System.out.println( "\tMEAN, NORMAL DELAY            			      " + x);
System.out.println( "\tSTANDARD DEVIATION, NORMAL DELAY               " + y);
System.out.println( "\tNUMBER OF PACKETS SERVED                       " + TotalPackets );
System.out.println( "\tSERVER UTILIZATION                             " + RHO );
System.out.println( "\tAVERAGE RESPONSE TIME                          " + AVGR);
System.out.println( "\tPROPORTION WHO SPEND FOUR "); 
System.out.println( "\t MINUTES OR MORE IN SYSTEM                     " + PC4 );
System.out.println( "\tSIMULATION RUNLENGTH                           " + Clock);
System.out.println( "\tNUMBER OF DEPARTURES                           " + TotalPackets );

System.out.println( "\tNUMBER OF Out-of-Order packets                 " + outorderCount );
System.out.println( "\tNUMBER OF Loss packets                         " + loss );

System.out.println( "\tNUMBER OF busy                                 " + busy);
System.out.println( "\tNUMBER OF idle                                 " + idle);
System.out.println( "\tNUMBER OF highq                                " + hc);
System.out.println( "\tNUMBER OF lowq                                 " + lc);

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

