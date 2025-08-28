# 이진 트리(Binary Tree)

## 0. 한눈에 보기(초간단)
- 트리(Tree) 중 각 정점이 최대 2개의 자식을 갖는 자료구조.
- 형태에 따라 포화/완전/편향 이진 트리로 구분.
- 활용: 힙(Heap), 이진 탐색 트리(BST) 등 핵심 자료구조의 기반.
- 순회 방식: 중위(in-order), 전위(pre-order), 후위(post-order), 층별(level-order).

---

## 1. 정의와 개요
- 트리는 방향성이 있는 비순환 그래프의 일종으로, 부모 정점 아래에 여러 자식 정점이 연결되는 계층적 자료구조입니다.
- 이진 트리(Binary Tree)는 각 정점(node)이 가질 수 있는 자식의 최대 개수가 2(왼쪽/오른쪽)인 트리입니다.

용어
- 루트(root): 트리의 최상위 정점
- 리프(leaf): 자식이 없는 정점
- 간선(edge): 정점과 정점을 잇는 연결
- 높이(height): 루트에서 가장 깊은 리프까지의 최장 경로 길이(레벨-1)
- 레벨(level): 루트를 1 또는 0으로 두고 아래로 갈수록 1씩 증가

---

## 2. 이진 트리의 종류
대표적으로 정점이 채워진 형태에 따라 다음과 같이 구분합니다.

1) 포화 이진 트리 (Perfect/Saturated Binary Tree)
- 마지막 레벨까지 모든 정점이 채워져 있는 경우

        1                --- level 1
      /   \
    2       3            --- level 2
   / \\     / \\
  4   5   6   7          --- level 3

2) 완전 이진 트리 (Complete Binary Tree)
- 마지막 레벨을 제외한 모든 레벨이 가득 차 있고, 마지막 레벨은 왼쪽부터 채워진 경우

        1                --- level 1
      /   \
    2       3            --- level 2
   / \\     /
  4   5   6              --- level 3

3) 편향 이진 트리 (Skewed Binary Tree)
- 한 방향(왼쪽 또는 오른쪽)으로만 정점이 이어지는 경우

    1                    --- level 1
     \
      2                  --- level 2
       \
        3                --- level 3
         \
          4              --- level 4
           \
            5            --- level 5

---

## 3. 특징과 활용
특징
- 정점이 N개인 경우, 최악의 경우(편향 트리) 높이는 N-1이 될 수 있습니다.
- 포화/완전 이진 트리의 높이는 대략 O(log N)입니다.
- 높이가 h인 포화 이진 트리는 총 정점 수가 2^(h + 1) - 1개입니다.

활용 사례
- 힙(Heap): 완전 이진 트리를 배열로 표현해 우선순위 큐를 구현.
- 이진 탐색 트리(BST): 왼쪽 서브트리 < 루트 < 오른쪽 서브트리 규칙으로 정렬/탐색 지원.
  - 균형 잡힌 BST(AVL, Red-Black 등)는 평균/최악 모두 O(log N) 탐색/삽입/삭제.
  - 불균형(편향)해지면 O(N)까지 성능 저하 → 균형 유지 전략 필요.
- 표현/파싱: 수식 트리(Expression Tree), 파싱 트리(Parse Tree) 등.

---

## 4. 이진 트리의 순회(Traversal)
일반적인 방문 순서는 다음 네 가지가 있습니다.

1) 중위 순회 (In-order)
- 순서: 왼쪽 → 부모 → 오른쪽
- BST에서 중위 순회 결과는 오름차순 정렬된 키 열이 됩니다.

2) 전위 순회 (Pre-order)
- 순서: 부모 → 왼쪽 → 오른쪽
- 트리의 복제/직렬화(구조 포함) 등에 자주 사용됩니다.

3) 후위 순회 (Post-order)
- 순서: 왼쪽 → 오른쪽 → 부모
- 하위 연산을 모두 처리한 뒤 부모를 처리할 때 유용(예: 서브트리 삭제).

4) 층별 순회 (Level-order, BFS)
- 레벨 1부터 시작해 각 레벨의 모든 노드를 왼쪽→오른쪽 순서로 방문 후 다음 레벨로 이동.
- 큐(Queue)를 사용한 너비 우선 탐색(BFS)으로 구현.

간단 예시 (트리: 1-2-4-5-3-6-7 구조)
- In-order:   4, 2, 5, 1, 6, 3, 7
- Pre-order:  1, 2, 4, 5, 3, 6, 7
- Post-order: 4, 5, 2, 6, 7, 3, 1
- Level-order:1, 2, 3, 4, 5, 6, 7

---

## 5. 면접 팁
- “완전 이진 트리” vs “포화 이진 트리” 정의를 정확히 구분하세요.
- BST 성능이 O(log N)이 되려면 “균형”이라는 전제가 필요합니다.
- 편향 트리는 연결 리스트와 유사한 높이를 가지므로 최악의 시간복잡도가 O(N)까지 악화됩니다.
- Level-order는 BFS이고, 큐를 사용한다는 연결고리를 분명히 설명하세요.

---

## 6. 참고
- 힙 정렬, 우선순위 큐 구현에서 완전 이진 트리의 배열 표현이 널리 쓰입니다.
- 균형 이진 탐색 트리: AVL, Red-Black, Treap, Splay 등.

---

## 7. Java 예시

### 7.1 기본 노드 정의와 샘플 트리 구성
```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

class BinaryTreeExamples {
    // 샘플 트리
    //        1
    //      /   \
    //     2     3
    //    / \
    //   4   5
    static TreeNode sampleTree() {
        TreeNode n1 = new TreeNode(1);
        TreeNode n2 = new TreeNode(2);
        TreeNode n3 = new TreeNode(3);
        TreeNode n4 = new TreeNode(4);
        TreeNode n5 = new TreeNode(5);
        n1.left = n2; n1.right = n3;
        n2.left = n4; n2.right = n5;
        return n1;
    }
}
```

### 7.2 순회 구현 (재귀/큐)
```java
import java.util.*;

class Traversal {
    static void inorder(TreeNode root, List<Integer> out) {
        if (root == null) return;
        inorder(root.left, out);
        out.add(root.val);
        inorder(root.right, out);
    }
    static void preorder(TreeNode root, List<Integer> out) {
        if (root == null) return;
        out.add(root.val);
        preorder(root.left, out);
        preorder(root.right, out);
    }
    static void postorder(TreeNode root, List<Integer> out) {
        if (root == null) return;
        postorder(root.left, out);
        postorder(root.right, out);
        out.add(root.val);
    }
    static List<Integer> levelOrder(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) return res;
        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            TreeNode cur = q.poll();
            res.add(cur.val);
            if (cur.left != null) q.offer(cur.left);
            if (cur.right != null) q.offer(cur.right);
        }
        return res;
    }

    public static void main(String[] args) {
        TreeNode root = BinaryTreeExamples.sampleTree();
        List<Integer> in = new ArrayList<>();
        List<Integer> pre = new ArrayList<>();
        List<Integer> post = new ArrayList<>();
        inorder(root, in);
        preorder(root, pre);
        postorder(root, post);
        List<Integer> level = levelOrder(root);
        System.out.println("In-order   = " + in);   // [4, 2, 5, 1, 3]
        System.out.println("Pre-order  = " + pre);  // [1, 2, 4, 5, 3]
        System.out.println("Post-order = " + post); // [4, 5, 2, 3, 1]
        System.out.println("Level-order= " + level);// [1, 2, 3, 4, 5]
    }
}
```

### 7.3 이진 탐색 트리(BST) 기본 연산
```java
class BST {
    TreeNode root;

    public void insert(int val) {
        root = insertRec(root, val);
    }
    private TreeNode insertRec(TreeNode node, int val) {
        if (node == null) return new TreeNode(val);
        if (val < node.val) node.left = insertRec(node.left, val);
        else if (val > node.val) node.right = insertRec(node.right, val);
        // 동일 값은 무시(집합 성질) 또는 카운트 증가 등 정책 선택 가능
        return node;
    }

    public boolean search(int val) {
        TreeNode cur = root;
        while (cur != null) {
            if (val == cur.val) return true;
            cur = (val < cur.val) ? cur.left : cur.right;
        }
        return false;
    }

    public void delete(int val) {
        root = deleteRec(root, val);
    }
    private TreeNode deleteRec(TreeNode node, int val) {
        if (node == null) return null;
        if (val < node.val) node.left = deleteRec(node.left, val);
        else if (val > node.val) node.right = deleteRec(node.right, val);
        else {
            // 케이스 1: 자식 0
            if (node.left == null && node.right == null) return null;
            // 케이스 2: 자식 1
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            // 케이스 3: 자식 2 -> 오른쪽 서브트리 최소값(후계자)로 대체
            TreeNode succ = node.right;
            while (succ.left != null) succ = succ.left;
            node.val = succ.val;
            node.right = deleteRec(node.right, succ.val);
        }
        return node;
    }

    public static void main(String[] args) {
        BST bst = new BST();
        int[] nums = {5, 3, 7, 2, 4, 6, 8};
        for (int n : nums) bst.insert(n);
        System.out.println(bst.search(4)); // true
        System.out.println(bst.search(9)); // false
        bst.delete(7);
        // 삭제 이후에도 BST 속성 유지
        List<Integer> in = new ArrayList<>();
        Traversal.inorder(bst.root, in);
        System.out.println(in); // [2, 3, 4, 5, 6, 8]
    }
}
```

참고
- 본 예시는 가독성을 위해 에러 처리/제네릭 생략. 실무에서는 제네릭, 널 처리, 불변성, 테스트 코드 등을 고려하세요.
- 균형 유지가 필요한 경우 AVL/Red-Black Tree 같은 균형 트리를 사용하세요.
