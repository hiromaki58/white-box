# はじめに
最短経路を求めるダイクストラ法を使って[LeetCode 743]("https://leetcode.com/problems/network-delay-time/description/")を解きたいと思います。
# この記事で実施する内容
1, Leetcode 743. Network Delay Timeの課題の内容
2, ダイクストラ法とは
3, コード
4, コードの解説
5, 最後に
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
ただ、ダイクストラ法は辺の重みが非負である場合に正しく動作します。

743番の問題では
1, 各ノードまでの最短時間を記録する配列を、始点 k の時間を0に、それ以外を無限大にする初期化。
2, スタート地点 k に隣接する頂点への移動時間を最短時間を記録する配列に記録する
3, さらに隣接する頂点への移動時間とこれまでの移動時間を合計し、最短時間を記録する配列に保存されている値よりも小さければ更新する。
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
# 4, コードの解説
グラフ構造を保持する Data クラスは2つの役割を担っています。
1, 隣接するノードと、そのノードへの移動時間を保持する。
2, キューでの処理を始めるために、スタート地点 k を、開始ノード k を距離 0 の状態でキューに入れて探索を始める。
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
グラフ構造を配列に格納しますが、全ての頂点がtimesに格納されてない可能性があるので以下の形で初期化します。
```java
List<List<Data>> neighbors = new ArrayList<>();
for(int i = 0; i < n; i++){
    neighbors.add(new ArrayList<>());
}
```
次にtimesに格納されている頂点を配列に格納します。
ここでは neighbors の添字が辺の始点、neighbors に含まれる Data の nextNode が辺の終点、Data の travelTime が辺の移動時間です。
```java
for(int[] time : times){
        neighbors.get(time[0] - 1).add(new Data(time[1] - 1, time[2]));
}
```
その頂点への最短時間を記録する配列です。
初期値は始点になる k をゼロに、それ以外を最大にします。
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
「k からその頂点への累積時間」 + 「次の頂点への移動時間」が「今わかっている隣接する頂点への最短時間」よりも短ければ更新し、さらにキューに追加します。
そのため、Data currPoint = que.poll();で最短距離が最も小さい候補を取り出すことができ、currPoint.travelTimeがこれまでの累積距離になります。
```java
while(!que.isEmpty()){
    Data currPoint = que.poll();                                            // 1) 現時点で最短距離が最も小さい候補を取り出す
    int currNode = currPoint.nextNode;
    int soFarTravelTime = currPoint.travelTime;                             // 出発点から currNode までの“累積距離”
    for(Data neighbor : neighbors.get(currNode)){                           // 2) currNode の隣接ノードへ
        int nextNode = neighbor.nextNode;
        int travelTime = neighbor.travelTime;                               // currNode → nextNode の辺の重み
        if(accumulatedTravelTime[nextNode] > soFarTravelTime + travelTime){ // 3) 別の経路の距離が短ければ置き換える
            accumulatedTravelTime[nextNode] = soFarTravelTime + travelTime; // 短ければ更新
            que.offer(new Data(nextNode, accumulatedTravelTime[nextNode])); // 4) 新しい距離で再度候補へ
        }
    }
}
````
accumulatedTravelTime には初期値で最大値を設定しているので、ひとつでも最大値のままの頂点があれば、 k からは到達できない頂点があることを意味します。
そうでなければ最大の値を返します。
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
# 5, 最後に
グラフ理論で頂点を配列に格納する場合、添え字がその頂点を示すことが理解のポイントになると思います。

また仮に優先度付キューを使わず線形探索を行った場合、すべての頂点からすべての隣接する頂点への距離を比較するのは同じですが、
線形探索では時間計算量がO(V^2)。
優先度付キューでは基本的な操作が(log V)を、最小値の取り出しを頂点の数だけ、更新を辺の数だけそれぞれ行うので、計算量はO((V + E) log V) になります。
