# はじめに
最短経路を求めるダイクストラ法を使って[LeetCode 743]("https://leetcode.com/problems/network-delay-time/description/")を解きたいと思います。
# この記事で実施する内容
1, Leetcode 743. Network Delay Timeの課題の内容
2, ダイクストラ法とは
3, コード
4, コードの解説
# 1, Leetcode 743. Network Delay Timeの課題の内容
この問題では n 個の頂点からなるネットワークを与えられます。ノードは 1 から n まで番号がついています。
times というリストが与えられ、それぞれの要素 times[i] = (ui, vi, wi) は、
ui: 出発ノード
vi: 到着ノード
wi: 信号が ui から vi に届くのにかかる時間（正の整数）
を持つ有向グラフです。
そして、特定のノード k から信号を送ります。
この条件ですべてのノード（1〜n）が信号を受け取るのにかかる最小の時間を求めてください。

信号がすべてのノードに届かない場合は -1 を返してください。
これは与えられたノードがすべて繋がっているとは限らないことを示しています。
# 2, ダイクストラ法とは
ダイクストラ法は、重み付きグラフにおいて、ある始点から各頂点への最小の重みを求めるアルゴリズムです。
幅優先探索に似た構造を持ちますが、異なる重みを扱うために優先度付きキュー(ヒープ)を使って、最も重みの小さなノードから順に処理します。
また処理中は、各ノードへの最小重み候補を配列で管理し、すでに確定したノードを別に記録します。
ただ、ダイクストラ法は非負でしか使えません。

743番の問題では
1, 各ノードまでの最短時間を記録する配列を、始点 k の時間を0に、それ以外を無限大にする初期化。
2, スタート地点 k に隣接する頂点への移動時間を最短時間を記録する配列に記録する
3, さらに隣接する頂点への移動時間とこれまでの移動時間を合計し、最短時間を記録する配列に保存されている値よりも小さければ講師する。
4, 隣接する頂点がある限りこれを繰り返す
5, 最短時間を記録する配列に、無限大の初期値が残ったままであれば k からは到達できない頂点があるため -1 を返す。
   そうでなければ最短時間を記録する配列のなかの最大値を返す
# 3, コード
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
        List<List<Data>> edges = new ArrayList<>();
        for(){
        	edges.add(new ArrayList<>());
        }

        for(int[] time : times){
        	edges.get(time[0] - 1).add(new Data(time[1] - 1, time[2]));
        }

        int[] accumulatedTravelTime = new int[n];
        Arrays.fill(accumulatedTravelTime, Integer.MAX_VALUE);
        accumulatedTravelTime[k - 1] = 0;

        PriorityQueue<Data> que = new PriorityQueue<>(Comparator.comparingInt(a -> a.soFarTravelTime));
        que.offer(new Data(k - 1, 0));

        while(!que.isEmpty()){
            Data currPoint = que.poll();
            int currNode = currPoint.nextNode;
            int soFarTravelTime = currPoint.travelTime;
            for(Data neighbor : edges.get(currNode)){
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
# 4, コードの解説
グラフ構造を保持する Data クラスは3つの役割を担っています。
1, 隣接するノードと、そのノードへの移動時間を保持する。
2, キューでの処理を始めるために、スタート地点 k を辺の終点として扱う。
3, その頂点への最短時間を保持する。
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
グラフ構造を配列に格納します。
ここでは edges の添字が辺の始点、edges に含まれる Data の nextNode が辺の終点、Data の travelTime が辺の移動時間です。
```java
List<List<Data>> edges = new ArrayList<>();
for(){
    pointers.add(new ArrayList<>());
}

for(int[] time : times){
    edges.get(time[0] - 1).add(new Data(time[1] - 1, time[2]));
}
```
最短時間を記録する配列を初期化、始点になる k 以外は移動時間を最大化します。
```java
int[] accumulatedTravelTime = new int[n];
Arrays.fill(accumulatedTravelTime, Integer.MAX_VALUE);
accumulatedTravelTime[k - 1] = 0;
```
移動時間を基準にした優先度付きキューと、そのキューに処理を始めるためスタート地点 k を辺の終点として格納します。
```java
PriorityQueue<Data> que = new PriorityQueue<>(Comparator.comparingInt(a -> a.soFarTravelTime));
que.offer(new Data(k - 1, 0));
````
キューに格納されている頂点の移動時間は、 k からその頂点への最短時間です。
「k からその頂点への最短時間」 + 「次の頂点への移動時間」が最短時間を記録する配列の値よりも少なければ更新し、さらにキューに格納します。
```java
while(!que.isEmpty()){
    Data currPoint = que.poll();
    int currNode = currPoint.nextNode;
    int soFarTravelTime = currPoint.travelTime;
    for(Data neighbor : edges.get(currNode)){
        int nextNode = neighbor.nextNode;
        int travelTime = neighbor.travelTime;
        if(accumulatedTravelTime[nextNode] > soFarTravelTime + travelTime){
            accumulatedTravelTime[nextNode] = soFarTravelTime + travelTime;
            que.offer(new Data(nextNode, accumulatedTravelTime[nextNode]));
        }
    }
}
````
accumulatedTravelTime には初期値で最大値を設定しているので、最大値のままであれば到達できないことを意味します。
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
