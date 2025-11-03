# Introduction
When looking at the family trees of European royalty, what often stands out is the frequency of marriages within the same family line—for example, cases where both husband and wife share the same great-grandmother.
If we think of a family tree as a tree structure, how can we determine when connections between nodes are duplicated?
In this article, we’ll use the Union-Find algorithm to solve LeetCode 684. Redundant Connection.
# What This Article Covers
1, The problem statement of Leetcode 684. Redundant Connection
2, What Union Find is
3, The code
4, Explanation of the code
5, Conclusion
# 1, The problem statement of Leetcode 684. Redundant Connection
In this problem, we are given an array of edges, where each edge is represented as a pair of two vertices in a two-dimensional array.
The graph provided is originally a tree (with n vertices and n-1 edges), but it has one extra edge added.
Since adding an extra edge to a tree always creates a cycle, the task is to find the edge that forms this cycle.
# 2, What Union Find is
This algorithm is based on two key operations:
1, Find – checks whether two elements belong to the same set.
2, Union – merges two elements into the same set if they belong together.

In this problem, the goal is to identify the edge that turns the structure into a cycle rather than a tree.
The find operation checks whether two vertices have the same parent (i.e., belong to the same set).
The union operation updates the parent reference in the array so that vertices with the same parent are grouped into the same set.

Union-Find is a data structure that manages the partitioning of sets. Internally, it is implemented by representing a forest (a collection of trees).
As a result, a parent array and a rank/level are used to efficiently merge sets.
# 3, The code
```java
class Solution {
    private int find(int x, int[] parent){
        if(parent[x] != x){
            parent[x] = find(parent[x], parent);
        }
        return parent[x];
    }

    private void union(int pu, int pv, int[] parent, int[] level){
        if(level[pu] < level[pv]){
            parent[pu] = pv;
        }
        else if(level[pu] > level[pv]){
            parent[pv] = pu;
        }
        else{
            parent[pv] = pu;
            level[pu]++;
        }
    }

    public int[] findRedundantConnection(int[][] edges) {
        int len = edges.length;
        int[] parent = new int[len + 1];
        int[] level = new int[len + 1];

        for(int i = 1; i <= len; i++){
            parent[i] = i;
            level[i] = 0;
        }

        for(int[] edge : edges){
            int u = edge[0];
            int v = edge[1];
            int pu = find(u, parent);
            int pv = find(v, parent);
            if(pu == pv){
                return edge;
            }
            union(pu, pv, parent, level);
        }
        return new int[0];
    }
}
```
# 4, Explanation of the code
```java
int len = edges.length;
int[] parent = new int[len + 1];
```
First, create an array to record the parent of each vertex.
As with Dijkstra’s algorithm in the previous article, the array index represents the vertex, and the stored value represents its parent.
The +1 offset is applied because the vertices start from 1.
```java
int[] level = new int[len + 1];

for(int i = 1; i < len; i++){
    parent[i] = i;
    level[i] = 0;
}
```
By initializing with parent[i] = i;, each vertex is set as its own parent.
An int[] level array is also prepared to record the height of the tree that each vertex’s set forms.
This allows us, when finding parents, to attach the shorter tree to the root of the taller tree.

Let’s start by explaining the find operation.
```java
private int find(int x, int[] parent){
    if(parent[x] != x){
        parent[x] = find(parent[x], parent);
    }
    return parent[x];
}
```
The find operation checks which vertex is the parent.
It’s easier to understand by drawing a tree on paper: when “index = vertex” and “stored value = parent” are different, it means that the vertex has another vertex as its parent.
In that case, it is also possible that this parent itself has yet another parent.
To trace this chain of parents, recursion is used.
```java
for(int[] edge : edges){
    int u = edge[0];
    int v = edge[1];
    int pu = find(u, parent);
    int pv = find(v, parent);
    if(pu == pv){
        return edge;
    }
    union(pu, pv, parent, level);
}
```
By applying find, the parent of each vertex belonging to an edge is checked one by one.
```java
if(pu == pv){
    return edge;
}
````
If the parents of two different vertices are the same, it means a cycle has been formed — in other words, that edge is the redundant one.
This is the answer we are looking for.
```java
private void union(int pu, int pv, int[] parent, int[] level){
    if(level[pu] < level[pv]){
        parent[pu] = pv;
    }
    else if(level[pu] > level[pv]){
        parent[pv] = pu;
    }
    else{
        parent[pv] = pu;
        level[pu]++;
    }
}
````
Next, let’s explain the union operation.
Since pu and pv are the two endpoints of the same edge, if one becomes the parent, the other becomes the child.
The union operation uses this to group vertices into sets with the same parent.
At the very beginning (the first iteration of for(int[] edge : edges)), the union process enters the else branch.
Because the goal is to form sets of vertices with the same parent, swapping the order of p and u still works correctly.
```java
level[pu]++;
```
Inside the else branch, the height of the parent vertex is incremented.
This records the height of the tree that the set forms. By doing so, the resulting trees remain relatively balanced, which helps keep the recursion depth of the find operation shallow.
# 5, Conclusion
Regarding complexity:
Without the level array, in the worst case, the union operations can form a straight-line tree.
In this case, the time complexity of find is O(n).

In contrast, when using level along with find (with path compression), the complexity becomes O(α(n)), where α(n) is the inverse Ackermann function.
This is because union with path compression ensures that repeated find calls have nearly constant average time complexity.
As a result, even if the number of vertices increases drastically, the time complexity hardly grows.
