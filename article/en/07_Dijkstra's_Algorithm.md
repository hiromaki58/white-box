# Introduction
I would like to solve[LeetCode 743: Network Delay Time]("https://leetcode.com/problems/network-delay-time/description/")using Dijkstra’s algorithm, which is designed to find the shortest paths.
# What This Article Covers
1, The problem statement of LeetCode 743: Network Delay Time
2, What Dijkstra’s algorithm is
3, The code
4, Explanation of the code
5, Conclusion
# 1, The problem statement of LeetCode 743: Network Delay Time
In this problem, we are given a network consisting of n nodes, numbered from 1 to n.
A list called times is provided, where each element times[i] = (ui, vi, wi) represents a directed edge:
ui: the starting node
vi: the target node
wi: the time it takes for a signal to travel from ui to vi (a positive integer)
A signal is sent from a given node k.
The goal is to determine the minimum time required for all nodes (1 through n) to receive the signal.

If it is impossible for all nodes to receive the signal, return -1.
This highlights that the graph may not necessarily be fully connected.
# 2, What Dijkstra’s algorithm is
Dijkstra’s algorithm is an algorithm for finding the minimum cost (shortest weight) from a starting node to all other nodes in a weighted graph.
It has a structure similar to breadth-first search, but because edge weights may differ, it uses a priority queue (heap) to always process the node with the smallest current weight.
During execution, the algorithm maintains an array of tentative shortest distances and keeps track of nodes whose shortest distances have already been finalized.

Note: Dijkstra’s algorithm works correctly only when all edge weights are non-negative.

For problem 743, the process can be summarized as follows:
1, Initialize an array to store the shortest times to each node: set the starting node k to 0, and all others to infinity.
2, Record the travel times to nodes adjacent to k in the shortest-time array.
3, For each adjacent node, add the cumulative travel time and update the shortest-time array if this value is smaller than the previously stored value.
4, Repeat this process while there are still unvisited adjacent nodes.
5, If any node in the shortest-time array still has the initial infinity value, it means it cannot be reached from k, so return -1. Otherwise, return the maximum value in the shortest-time array (the time when the last node receives the signal).
# 3, The code
```java
class Data{
    int nextNode;
    int travelTime;

    public Data(int nextNode, int travelTime){
        this.nextNode = nextNode;
        this.travelTime = travelTime;
    }
}
class Solution {
    public int networkDelayTime(int[][] times, int n, int k) {
        List<List<Data>> neighbors = new ArrayList<>();
        for(int i = 0; i < n; i++){
            neighbors.add(new ArrayList<>());
        }

        for(int[] time : times){
            neighbors.get(time[0] - 1).add(new Data(time[1] - 1, time[2]));
        }

        int[] accumulatedTravelTime = new int[n];
        Arrays.fill(accumulatedTravelTime, Integer.MAX_VALUE);
        accumulatedTravelTime[k - 1] = 0;

        PriorityQueue<Data> que = new PriorityQueue<>(Comparator.comparingInt(a -> a.travelTime));
        que.offer(new Data(k - 1, 0));

        while(!que.isEmpty()){
            Data currPoint = que.poll();
            int currNode = currPoint.nextNode;
            int soFarTravelTime = currPoint.travelTime;
            for(Data neighbor : neighbors.get(currNode)){
                int nextNode = neighbor.nextNode;
                int travelTime = neighbor.travelTime;
                if(accumulatedTravelTime[nextNode] > soFarTravelTime + travelTime){
                    accumulatedTravelTime[nextNode] = soFarTravelTime + travelTime;
                    que.offer(new Data(nextNode, accumulatedTravelTime[nextNode]));
                }
            }
        }

        int maxTime = 0;
        for(int time : accumulatedTravelTime){
            if(time == Integer.MAX_VALUE){
                return -1;
            }
            maxTime = Math.max(maxTime, time);
        }
        return maxTime;
    }
}
```
# 4, Explanation of the code
The Data class, which holds the graph structure, serves two main purposes:
1, To store adjacent nodes along with the travel time to those nodes.
2, To initialize the queue by placing the starting node k into it with a distance of 0, so that the exploration can begin.
```java
class Data{
    int nextNode;
    int travelTime;

    public Data(int nextNode, int travelTime){
        this.nextNode = nextNode;
        this.travelTime = travelTime;
    }
}
```
Although the graph structure is stored in an array, not all vertices may appear in the times input.
Therefore, we initialize the graph in the following way.
```java
List<List<Data>> neighbors = new ArrayList<>();
for(int i = 0; i < n; i++){
    neighbors.add(new ArrayList<>());
}
```
Next, we insert the vertices from times into the array.
Here, the index of the neighbors array represents the starting point of an edge, the nextNode field of the Data object represents the endpoint of the edge, and the travelTime field represents the weight of the edge.
```java
for(int[] time : times){
        neighbors.get(time[0] - 1).add(new Data(time[1] - 1, time[2]));
}
```
We also maintain an array to record the shortest travel time to each vertex.
The initial values are set to 0 for the starting node k and infinity for all others.
```java
int[] accumulatedTravelTime = new int[n];
Arrays.fill(accumulatedTravelTime, Integer.MAX_VALUE);
accumulatedTravelTime[k - 1] = 0;
```
A priority queue is used, based on travel time, and the starting node k is added to it to begin processing.
```java
PriorityQueue<Data> que = new PriorityQueue<>(Comparator.comparingInt(a -> a.soFarTravelTime));
que.offer(new Data(k - 1, 0));
````
If
“the cumulative travel time from k to the current node” + “the travel time to the next node”
is less than
“the shortest travel time currently known for that adjacent node,”
then we update the value and add the node to the queue.
Thus, by calling Data currPoint = que.poll();, we can always retrieve the candidate with the smallest distance.
Here, currPoint.travelTime represents the cumulative distance up to that node.
```java
while(!que.isEmpty()){
    Data currPoint = que.poll();                                            // 1) Extract the candidate with the smallest current shortest distance
    int currNode = currPoint.nextNode;
    int soFarTravelTime = currPoint.travelTime;                             // This represents the cumulative distance from the starting point to currNode.
    for(Data neighbor : neighbors.get(currNode)){                           // 2) Move to the adjacent nodes of currNode
        int nextNode = neighbor.nextNode;
        int travelTime = neighbor.travelTime;                               // Consider the edge weight from currNode → nextNode.
        if(accumulatedTravelTime[nextNode] > soFarTravelTime + travelTime){ // 3) Replace if an alternative path is shorter
            accumulatedTravelTime[nextNode] = soFarTravelTime + travelTime; // If the new path is shorter, update the value.
            que.offer(new Data(nextNode, accumulatedTravelTime[nextNode])); // 4) Reinsert the updated node as a new candidate
        }
    }
}
````
Since the accumulatedTravelTime array is initialized with infinity, if any vertex remains with that value, it means the vertex is unreachable from k, and the algorithm should return -1.
Otherwise, return the maximum value in the array.
```java
int maxTime = 0;
for(int time : accumulatedTravelTime){
    if(time == Integer.MAX_VALUE){
        return -1;
    }
    maxTime = Math.max(maxTime, time);
}
return maxTime;
```
# 5, Conclusion
When working with graph theory, one key point to remember is that if vertices are stored in an array, their indices correspond to the vertex identifiers.

If we were to use a linear search instead of a priority queue, we would still be comparing distances from every vertex to all its neighbors.
However:
Linear search: time complexity is O(V²).
Priority queue: each fundamental operation is O(log V). Extracting the minimum is performed V times, and updating distances happens E times.
Therefore, the overall time complexity is O((V + E) log V).
