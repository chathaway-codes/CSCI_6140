/********************************************************************/
/*********************************** File header.h ******************/
/********************************************************************/
/* for random number generator */
#define Nrnd 624
#define Mrnd 397
#define MATRIX_A 0x9908b0dfUL   /* constant vector a */
#define UPPER_MASK 0x80000000UL /* most significant w-r bits */
#define LOWER_MASK 0x7fffffffUL /* least significant r bits */

/***** Define simulation *****/
#define MS 20
#define NS 100
#define NPP 6

#define TCPU (0.04/START_AMAT*AMAT)
#define TQuantum 0.1
#define TInterRequest (0.016/START_AMAT*AMAT)
#define TDiskService 0.01
// Monitor thinking time
#define TThink 5
// Context switching time
#define CPU_SWITCH .0005

// Time Barrier Synchronization time
#define TBS (0.4/START_AMAT*AMAT)
#define TTS 20000

// Total system memory
#define TOTAL_MEM 8192
// OS required RAM
#define OS_RAM 512
// Available ram
#define AVAIL_RAM (TOTAL_MEM-OS_RAM)
#define M (AVAIL_RAM/MPL)

// CPU instruction time
#define CPU_INST_TIME 1E-9

#define MemoryQueue 0
#define CPUQueue 1
#define DiskQueue 2
#define TotQueues 3
#define RequestMemory 0
#define RequestCPU 1
#define ReleaseCPU 2
#define RequestDisk 3
#define ReleaseDisk 4

#define CPU 0
#define NUM_CPU 4

#define DISK (CPU + NUM_CPU)
#define NUM_DISK 1

#define EMPTY -1
#define LowPriority 0
#define HighPriority 1

/********** Calculate AMAT **********/
#define START_CACHE_MISS .02
#define START_CACHE_MISS_COST 51.0
#define START_AMAT (1.0+START_CACHE_MISS*(START_CACHE_MISS_COST-1.0))

#define CACHE_MISS .02
#define CACHE_MISS_COST 51.0
#define AMAT (1.0+CACHE_MISS*(CACHE_MISS_COST-1.0))


#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

/* for random number generator */
static unsigned long mt[Nrnd];     /* the array for the state vector  */
static int mti=Nrnd+1;             /* mti==Nrnd+1 means mt[Nrnd] is not initialized */

/* simulator data structurs */
struct Task {
    double tcpu,tquantum,tinterrequest,tbs,tpgf,start;
    int cpu,disk,waiting;
} task[NS + NPP];  /**** Job list       ****/
struct Events {                              /**** Event list           ****/
    int head, tail, q;
    double time[NS+NPP];
    int task[NS+NPP], event[NS+NPP];
} elist;
struct Queue {  /**** Queues: 0 - memory queue, 1 - CPU queue, 2 - Disk queue*/
    int head, tail, q, n, task[NS+NPP];
    double ws, tch, ts, tentry[NS+NPP];
} queue[TotQueues];
struct Device {                              /***  Devices: 0 - CPU, 1 - Disk*/
    int busy;
    double tch, tser;
    double idle_time_1, idle_time_2;
} server[NUM_CPU + NUM_DISK];

// # proc. in mem, num finished, MPL, N of monitors
int inmemory=0, finished_tasks=0, finished_pp_tasks=0;
int MPL=MS, N=NS;
int cur_disk=0;
double sum_response_time=0.0, TTotal=TTS;
void Process_RequestMemory(int, double);
void Process_RequestCPU(int, double);
void Process_ReleaseCPU(int, double);
void Process_RequestDisk(int, double);
void Process_ReleaseDisk(int, double);
double random_exponential(double);
void place_in_queue(int, double, int);
void create_event(int, int, double, int);
void init();
void stats();
void stats2();
int remove_from_queue(int, double);

/* for random number generator */
unsigned long genrand_int32(void);
double genrand_real2(void);

void create_process(int process, double time);
double calc_tip();
void set_next_page_fault(int process, double time);


/********************************************************************/
/********************** File sim.c **********************************/
/* can be separated from header.c by using include below  ***********/
/* #include "header.h"                                             **/
void main(int argc, char *argv[])
{
    double global_time=0.0, last_time=0.0;
    int process, event, counter=0, i=0;

    /* read in parameters */
    if (argc>1) sscanf(argv[1], "%d", &MPL);
    if (argc>2) sscanf(argv[2], "%d", &N);
    if (N>NS) {
        printf("Inline N is larger than NS, increase NS so data structures are of right size\n");
        exit(1);
    }
    if (argc>3) sscanf(argv[3], "%lf", &TTotal);

    init();
    /***** Main simulation loop *****/
    while (global_time<=TTotal) {
        /***** Select the event e from the head of event list *****/
        process=elist.task[elist.head];
        global_time = elist.time[elist.head];
        event = elist.event[elist.head];
        elist.head=(elist.head+1)%(NS+NPP);
        elist.q--;

        //printf("Working on %d at time %.15lf\n", process, global_time);
        switch(event) {
        case RequestMemory:
            Process_RequestMemory(process, global_time);
            break;
        case RequestCPU:
            Process_RequestCPU(process, global_time);
            break;
        case ReleaseCPU:
            Process_ReleaseCPU(process, global_time);
            break;
        case RequestDisk:
            Process_RequestDisk(process, global_time);
            break;
        case ReleaseDisk:
            Process_ReleaseDisk(process, global_time);
        }

        // If the disk queue is not empty, search for spare processors
        //  and add time to the idle time
        if(queue[2].q != 0) {
            for(i=CPU; i < CPU+NUM_CPU; i++) {
                if(!server[i].busy &&
                    (task[process].cpu != i && event != ReleaseCPU))
                    server[i].idle_time_1 += global_time - last_time;
            }
        }

        // If all the queues (except eq) are empty,
        //  record time if this processor is idle
        if(queue[1].q == 0 && queue[2].q == 0) {
            for(i=CPU; i < CPU+NUM_CPU; i++) {
                if(!server[i].busy &&
                    (task[process].cpu != i && event != ReleaseCPU))
                    server[i].idle_time_2 += global_time - last_time;
            }
        }

        last_time = global_time;

        counter++;
        if(counter % 100000 == 0)
            printf("Time: %lf\n", global_time);
    }
    stats();
}

/********************************************************************/
/********************* Event Functions ******************************/
/********************************************************************/

void Process_RequestMemory(int process, double time)
{
    if (inmemory<MPL) {
        inmemory++;
        create_event(process, RequestCPU, time, LowPriority);
    }
    else place_in_queue(process, time, MemoryQueue);
}

void Process_RequestCPU(int process, double time)
{
    double release_time;
    int i=0;

    for(i=CPU; i < CPU + NUM_CPU; i++) {
        if(!server[i].busy) {
            server[i].busy=1;
            server[i].tch=time;
            task[process].cpu = i;

            if (task[process].tcpu<task[process].tquantum) 
                release_time=task[process].tcpu;
            else 
                release_time=task[process].tquantum;

            if (release_time>task[process].tinterrequest) 
                release_time=task[process].tinterrequest;
            if (release_time>task[process].tpgf)
                release_time=task[process].tpgf;
            if (process>=NS && release_time>task[process].tbs)
                release_time=task[process].tbs;

            task[process].tcpu-=release_time;
            task[process].tinterrequest-=release_time;
            task[process].tquantum-=release_time;
            task[process].tpgf-=release_time;
            task[process].tbs-=release_time;
            create_event(process, ReleaseCPU, time+release_time+CPU_SWITCH, LowPriority);
            return;
        }
    }
    place_in_queue(process,time,CPUQueue);
}

void Process_ReleaseCPU(int process, double time)
{
    int queue_head,i=0;

    /**** Update CPU statistics                                            ****/
    server[task[process].cpu].busy=0;
    server[task[process].cpu].tser+=(time-server[task[process].cpu].tch-CPU_SWITCH);
    queue_head=remove_from_queue(CPUQueue, time);
    if (queue_head!=EMPTY) 
        create_event(queue_head, RequestCPU, time, HighPriority);

    if (task[process].tcpu==0) {
        assert(time > task[process].start);
        sum_response_time+=time-task[process].start;
        finished_tasks++;

        create_process(process, time+random_exponential(TThink));
        create_event(process, RequestMemory, task[process].start, LowPriority);
        inmemory--;
        queue_head=remove_from_queue(MemoryQueue, time);
        if (queue_head!=EMPTY) 
            create_event(queue_head, RequestMemory, time, HighPriority);
    }
    else if (task[process].tquantum==0) {
        task[process].tquantum=TQuantum;
        if(process < NS) {
            inmemory--;
            create_event(process, RequestMemory, time, LowPriority);
        }
        else
            create_event(process, RequestCPU, time, LowPriority);
    }
    else if (task[process].tbs==0) {
        // Task should be suspended
        task[process].waiting=1;
        finished_pp_tasks++;

        // If all the PP tasks are done, restart them
        for(i=NS; i < NS + NPP; i++) {
            if(task[i].waiting==0) return;
        }

        for(i=NS; i < NS + NPP; i++) {
            task[i].tbs=random_exponential(TBS);
            task[i].start=time;
            task[i].tcpu = TTS;
            task[i].waiting = 0;
            create_event(i, RequestCPU, task[i].start, LowPriority);
        }
    }
    else if (task[process].tpgf==0) {
        set_next_page_fault(process, time);
        create_event(process, RequestDisk, time, LowPriority);
    }
    else {
        task[process].tinterrequest=random_exponential(TInterRequest);
        create_event(process, RequestDisk, time, LowPriority);
    }
}

void Process_RequestDisk(int process, double time)
{
    /*int i=0, j=0;
    for(i=cur_disk, j=0; j < NUM_DISK; i = (cur_disk+j)%NUM_DISK, j++) {
        if(!server[i].busy) {
            server[i].busy = 1;
            server[i].tch = time;
            task[process].disk = i;
            create_event(process, ReleaseDisk, time+TDiskService, LowPriority);
            return;
        }
    }*/
    int disk=cur_disk+DISK;
    if(!server[disk].busy) {
        server[disk].busy = 1;
        server[disk].tch = time;
        task[process].disk = disk;
        cur_disk = (cur_disk+1)%NUM_DISK;
        create_event(process, ReleaseDisk, time+TDiskService, LowPriority);
        return;
    }
    place_in_queue(process,time,DiskQueue);
}

void Process_ReleaseDisk(int process, double time)
{
    int queue_head;
    server[task[process].disk].busy=0;
    server[task[process].disk].tser+=(time-server[task[process].disk].tch);
    queue_head=remove_from_queue(DiskQueue, time);
    if (queue_head!=EMPTY)
        create_event(queue_head, RequestDisk, time, HighPriority);
    create_event(process, RequestCPU, time, LowPriority);
}

/********************************************************************/
/******************* Auxiliary Functions ****************************/
/********************************************************************/

int remove_from_queue(int current_queue, double time)
{
    int process;

    /**** If queue not empty, remove the head of the queue              ****/
    if (queue[current_queue].q>0) {
        process=queue[current_queue].task[queue[current_queue].head];
        /**** Update statistics for the queue                               ****/
        queue[current_queue].ws+=
            time-queue[current_queue].tentry[queue[current_queue].head];
        queue[current_queue].ts+=
            (time-queue[current_queue].tch)*queue[current_queue].q;
        queue[current_queue].q--;
        queue[current_queue].tch=time;
        /**** Create a new event for the task at the head and move the head ****/
        queue[current_queue].head=(queue[current_queue].head+1)%(NS+NPP);
        return(process);
    }
    else return(EMPTY);
}

void place_in_queue(int process, double time, int current_queue)
{
    /**** Update statistics for the queue                               ****/
    queue[current_queue].ts+=
        (time-queue[current_queue].tch)*queue[current_queue].q;
    queue[current_queue].q++;
    queue[current_queue].n++;
    queue[current_queue].tch=time;
    /**** Place the process at the tail of queue and move the tail       ****/
    queue[current_queue].task[queue[current_queue].tail]=process;
    queue[current_queue].tentry[queue[current_queue].tail]=time;
    queue[current_queue].tail=(queue[current_queue].tail+1)%(NS+NPP);
}

void create_event(int process, int event, double time, int priority)
{
    int i, notdone=1, place=elist.tail;

    /**** Move all more futuristic tasks by one position                ****/
    for(i=(elist.tail+(NS+NPP)-1)%(NS+NPP); notdone && elist.q>0; i=(i+(NS+NPP)-1)%(NS+NPP)) {
        if(elist.time[i] < time || (priority==LowPriority && elist.time[i]==time))
            notdone=0;
        else {
            elist.time[place]=elist.time[i];
            elist.task[place]=elist.task[i];
            elist.event[place]=elist.event[i];
            place=i;
        }
        if (i==elist.head) notdone=0;
    }
    /**** Place the argument event in the newly created space           ****/
    elist.time[place]=time;
    elist.task[place]=process;
    elist.event[place]=event;
    elist.tail=(elist.tail+1)%(NS+NPP);
    elist.q++;
}

void init()
{
    int i;

    /**** Initialize structures                                         ****/
    elist.head=elist.tail=elist.q=0;
    for(i=0; i<TotQueues; i++) {
        queue[i].head=queue[i].tail=queue[i].q=queue[i].n=0;
        queue[i].ws=queue[i].ts=0.0;
        queue[i].tch=0;
    }
    for(i=0; i<NUM_CPU + NUM_DISK; i++) {
        server[i].busy=0;
        server[i].tch=server[i].tser=0.0;
        server[i].idle_time_1=server[i].idle_time_2=0;
    }
    for(i=0; i<N; i++) {
        create_process(i, random_exponential(TThink));
        create_event(i, RequestMemory, task[i].start, LowPriority);
    }

    for(i=NS; i < NPP+NS; i++) {
        create_process(i, 0);
        task[i].tcpu = TTS+1;
        create_event(i, RequestMemory, task[i].start, LowPriority);
    }
}

void stats()
{
    int i=0;
    /**** Update utilizations                                          ****/
    for(i=0; i < NUM_CPU + NUM_DISK; i++) {
        if(server[i].busy==1)
            server[i].tser+=(TTotal-server[i].tch);
    }

    printf("System definitions: N %2d MPL %2d NPP %2d CPUs %2d Disks %2d TTotal %6.0f\n",N, MPL, NPP, NUM_CPU, NUM_DISK, TTotal);
    printf("m %d amat %lf TIP %lf\n", M, AMAT, calc_tip());
    double total_cpu_util = 0.0;
    for(i=CPU; i < CPU + NUM_CPU; i++) {
        total_cpu_util += 100.0*server[i].tser/TTotal;
        printf("CPU%d: %5.2f idle %5.2f queues_empty %5.2f\n", i-CPU, 100.0*server[i].tser/TTotal,
            100.0*server[i].idle_time_1/TTotal, 100*server[i].idle_time_2/TTotal);
    }
    double total_disk_util = 0.0;
    for(i=DISK; i < DISK + NUM_DISK; i++) {
        total_disk_util += 100.0*server[i].tser/TTotal;
        printf("DISK%d: %5.2f\n", i-DISK, 100.0*server[i].tser/TTotal);
    }
    printf("utilizations are: CPU %5.2f Disk %5.2f\n",
    total_cpu_util/NUM_CPU, total_disk_util/NUM_DISK);
    printf("mean waiting time in qe %5.2f qCPU %5.2f qDisk %5.2f\n",
    queue[MemoryQueue].ws?queue[MemoryQueue].ws/(queue[MemoryQueue].n-queue[MemoryQueue].q):0.0,
    queue[CPUQueue].ws?queue[CPUQueue].ws/(queue[CPUQueue].n-queue[CPUQueue].q):0.0,
    queue[DiskQueue].ws?queue[DiskQueue].ws/(queue[DiskQueue].n-queue[DiskQueue].q):0.0);
    printf("mean queue length in qe %5.2f qCPU %5.2f qDisk %5.2f\n",
    queue[MemoryQueue].tch?queue[MemoryQueue].ts/queue[MemoryQueue].tch:0.0,
    queue[CPUQueue].tch?queue[CPUQueue].ts/queue[CPUQueue].tch:0.0,
    queue[DiskQueue].tch?queue[DiskQueue].ts/queue[DiskQueue].tch:0.0);
    printf("number of visits in qe %5d qCPU %5d qDisk %5d\n",
    queue[0].n-queue[0].q,
    queue[CPUQueue].n-queue[CPUQueue].q,
    queue[DiskQueue].n-queue[DiskQueue].q);
    printf("average response time %5.2f processes finished %5d parallel tasks finished %5d\n",
    sum_response_time/finished_tasks, finished_tasks, finished_pp_tasks);
    
    printf("sum_response_time: %lf\n", sum_response_time);

}

void stats2() {
    int i=0;
    /**** Update utilizations                                          ****/
    for(i=0; i < NUM_CPU + NUM_DISK; i++) {
        if(server[i].busy==1)
            server[i].tser+=(TTotal-server[i].tch);
    }

    printf("System definitions: N %2d MPL %2d NPP %2d CPUs %2d Disks %2d TTotal %6.0f\n",N, MPL, NPP, NUM_CPU, NUM_DISK, TTotal);
    //printf("m %d amat %d TIP %lf\n", M, 0, 0.0);
    double total_cpu_util = 0.0;
    for(i=CPU; i < CPU + NUM_CPU; i++) {
        total_cpu_util += 100.0*server[i].tser/TTotal;
        //printf("CPU%d: %5.2f\n", i-CPU, 100.0*server[i].tser/TTotal);
    }
    double total_disk_util = 0.0;
    for(i=DISK; i < DISK + NUM_DISK; i++) {
        total_disk_util += 100.0*server[i].tser/TTotal;
        //printf("DISK%d: %5.2f\n", i-DISK, 100.0*server[i].tser/TTotal);
    }

    printf("RT = Response Time\n");
    printf("CPU = CPU utilization by user' processes\n");
    printf("DISK = Disk utilization\n");
    printf("WAIT = Average waiting time in eq\n");

    printf("RT\t\tCPU\t\tDISK\t\tWAIT\n");
    printf("%lf\t%lf\t%lf\t%lf\n",
        sum_response_time/finished_tasks, total_cpu_util/NUM_CPU, total_disk_util/NUM_DISK,
        queue[MemoryQueue].ws?queue[MemoryQueue].ws/(queue[MemoryQueue].n-queue[MemoryQueue].q):0.0);
}

/*------------------------------ Random Number Generator --------------------------*/

void init_genrand(unsigned long s) {

    mt[0]= s & 0xffffffffUL;
    for (mti=1; mti<Nrnd; mti++) {
        mt[mti] = (1812433253UL * (mt[mti-1] ^ (mt[mti-1] >> 30)) + mti);
        mt[mti] &= 0xffffffffUL;
    }
}

unsigned long genrand_int32(void) {
    unsigned long y;
    static unsigned long mag01[2]= {0x0UL, MATRIX_A};
    if (mti >= Nrnd) {
        int kk;

        if (mti == Nrnd+1) init_genrand(5489UL);

        for (kk=0; kk<Nrnd-Mrnd; kk++) {
            y = (mt[kk]&UPPER_MASK)|(mt[kk+1]&LOWER_MASK);
            mt[kk] = mt[kk+Mrnd] ^ (y >> 1) ^ mag01[y & 0x1UL];
        }
        for (; kk<Nrnd-1; kk++) {
            y = (mt[kk]&UPPER_MASK)|(mt[kk+1]&LOWER_MASK);
            mt[kk] = mt[kk+(Mrnd-Nrnd)] ^ (y >> 1) ^ mag01[y & 0x1UL];
        }
        y = (mt[Nrnd-1]&UPPER_MASK)|(mt[0]&LOWER_MASK);
        mt[Nrnd-1] = mt[Mrnd-1] ^ (y >> 1) ^ mag01[y & 0x1UL];

        mti = 0;
    }

    y = mt[mti++];
    y ^= (y >> 11);
    y ^= (y << 7) & 0x9d2c5680UL;
    y ^= (y << 15) & 0xefc60000UL;
    y ^= (y >> 18);

    return y;
}

double genrand_real2(void) {
    return genrand_int32()*(1.0/4294967296.0);
}

double random_exponential (double y) {
    return -y*log(genrand_real2());
}

void create_process(int process, double time) {
    task[process].tcpu=random_exponential(TCPU);
    task[process].tquantum  =   TQuantum;
    task[process].tinterrequest = random_exponential(TInterRequest);
    task[process].tbs = random_exponential(TBS);
    task[process].disk = -1;
    task[process].cpu = -1;
    task[process].waiting = 0;
    task[process].start=time;

    set_next_page_fault(process, time);
}

double calc_tip() {
    double p = M/160.0 + 17;
    double fm = pow(2, -1*p);

    return (1/fm)*(CPU_INST_TIME)*AMAT;
}

void set_next_page_fault(int process, double time) {
    double p = M/160.0 + 17;
    double fm = pow(2, -1*p);
    // s / (number of instructions per second)
    //double one_fault_per = 1/fm;
    //task[process].tpgf = random_exponential(one_fault_per * CPU_INST_TIME)/5;
    
    task[process].tpgf = (1/fm)*(CPU_INST_TIME)*AMAT;

    //printf("Prob of a page fault: %.20lf\n", fm);
    //printf("Page faults occur ever %.20lf cycles\n", one_fault_per);
    //printf("process[%d] will have a page fault in %.20lfs\n", process, task[process].tpgf);
    //printf("p = %lf M=%d\n", p, M);
    /*printf("%lf*(1/%.10lf)*%.20lf\n", task[process].tcpu, CPU_INST_TIME, fm);
    printf("Process[%d].tcpu = %.20lf\n", process, task[process].tcpu);
    printf("Process[%d] will have %.20lf page faults\n", process, task[process].tcpu*(1/CPU_INST_TIME)*fm);*/
}
