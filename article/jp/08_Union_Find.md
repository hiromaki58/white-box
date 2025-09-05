# はじめに
ヨーロッパの王族の家系図を見ると、夫婦両方の曽祖母が同じ人であるなど、同族での結婚の多さが目を惹きます。
家系図を木構造と考えた時に、頂点と頂点のつながりが重複してことを調べるにはどうすればいいのでしょうか？
今回はUnion Find法を使って[LeetCode 684. Redundant Connection]("https://leetcode.com/problems/redundant-connection/description/")を解きたいと思います。
# この記事で実施する内容
1, Leetcode 684. Redundant Connectionの課題の内容
2, Union Findとは
3, コード
4, コードの解説
5, 最後に
# 1, Leetcode 684. Redundant Connectionの課題の内容
この問題では、二つの頂点がセットになった要素が格納された多次元配列で示された辺の配列が与えられます。
与えられるグラフはもともと木構造（n 頂点と n-1 辺）ですが、そこに 1 本余分な辺が追加されています。
木に余分な辺を加えると必ずサイクルができるため、そのサイクルを作る辺を探すのが課題です。
# 2, Union Findとは
このアルゴリズムは2つの考えから構成されます。
1, その要素が同じ集合に属するかを調べる find
2, 同じ集合に属している要素を一つにする union

この問題の場合木ではなくサイクルにしてしまう辺を探しているので、 
その頂点の親が同じ頂点 = 同じ集合、であることを find が確かめ
union が配列の親頂点の番号を書き換えることで、同じ親を持つ頂点を同じ集合にまとめます。

Union-Find は集合の分割を管理するデータ構造で、内部的に森（複数の木）を表現して実装されています。
そのため parent 配列と level を使って集合を効率的に統合できます。
# 3, コード
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
# 4, コードの解説
```java
int len = edges.length;
int[] parent = new int[len + 1];
```
まずそれぞれの頂点の親がどの頂点なのかを記録する配列を作成します。
前回のダイクストラと同様に、配列の添え字が頂点を、格納される値が親を意味します。
+1 しているのは、頂点が 1 からスタートしているからです。
```java
int[] level = new int[len + 1];

for(int i = 1; i < len; i++){
    parent[i] = i;
    level[i] = 0;
}
```
parent[i] = i; によって各頂点の親をその頂点自身とすることで配列を初期化します。
int[] level はその頂点が属する集合が構成する木の高さを記録します。
これを使って親を見つけた時に、根から葉までの距離が長い木に、根から葉までの距離が短い木をつなげます。

まず find から説明しましょう。
```java
private int find(int x, int[] parent){
    if(parent[x] != x){
        parent[x] = find(parent[x], parent);
    }
    return parent[x];
}
```
find は親がどの頂点かを調べます。
木構造を紙に書くと理解しやすいですが、「添え字 = それぞれの頂点」と「格納された要素 = 親」が異なっている場合、
つまりその頂点が別の頂点を親として持つ場合、その頂点にはさらに別の親がいる可能性があります。
それを調べるために再帰しています。
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
find を使ってそれぞれの辺が持つ頂点の親がどの頂点かを順番に調べます。
```java
if(pu == pv){
    return edge;
}
````
2つの異なる頂点の親が同じということは、サイクルが生じてる = その辺が重複した辺であることを意味します。
これが答えです。
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
次に union を説明しましょう。
pu と pv は同じ辺の両端であることから、片方が親であればもう片方は子になることを利用して、 union は同じ親を持つ集合を作っていきます。
最も初期の段階 for(int[] edge : edges) の一回目、 union は else になります。
同じ親を持つ頂点の集合を作成するのが目的なので、 p と u の位置を逆にしても問題なく機能します。
```java
level[pu]++;
```
else の中で親になる頂点の高さを一つ増やします。
こうすることである頂点の親頂点の高さを記録し、その集合が構成する木の高さを記録します。
そうすると、ある程度バランスが整った木を作ることができ、 find での再帰の深さを浅くすることができます。
# 5, 最後に
計算量に関してですが、もしも level が無い場合、最悪一直線の木が出来上がります。
この時の find の時間計算量は O(n) になります。 

対して level と find(経路圧縮) を使うと、頂点が n 個として O(α(n)) になります。
これは union を使ってパス圧縮をすると、繰り返し find を呼んだ時の平均計算量はほぼ定数であること。
そのため頂点の数が極端に増えても、時間計算量はあまり増えない、逆アッカーマン関数になります。
