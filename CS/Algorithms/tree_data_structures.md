# 트리(Tree) 데이터 구조

트리는 계층적 관계를 표현하는 비선형 자료구조로, 노드(node)들과 노드들을 연결하는 간선(edge)들로 구성됩니다. 트리는 사이클이 없는 연결 그래프의 일종입니다.

## 트리의 기본 용어

- **노드(Node)**: 트리를 구성하는 기본 요소
- **간선(Edge)**: 노드와 노드를 연결하는 선
- **루트 노드(Root Node)**: 트리의 최상위에 있는 노드
- **부모 노드(Parent Node)**: 특정 노드의 상위 노드
- **자식 노드(Child Node)**: 특정 노드의 하위 노드
- **형제 노드(Sibling Node)**: 같은 부모를 가진 노드
- **리프 노드(Leaf Node)**: 자식이 없는 노드
- **내부 노드(Internal Node)**: 적어도 하나의 자식을 가진 노드
- **경로(Path)**: 한 노드에서 다른 노드로 이동하기 위한 간선의 순서

## 트리의 속성

### 높이(Height)와 깊이(Depth)

- **노드의 깊이(Depth)**: 루트 노드에서 해당 노드까지의 경로 길이
- **노드의 높이(Height)**: 해당 노드에서 가장 먼 리프 노드까지의 경로 길이
- **트리의 높이(Height)**: 루트 노드의 높이, 즉 트리에서 가장 깊은 노드의 깊이

### 차수(Degree)

- **노드의 차수**: 노드가 가진 자식 노드의 수
- **트리의 차수**: 트리에 있는 노드들 중 최대 차수

## 트리의 종류

### 이진 트리(Binary Tree)

모든 노드가 최대 2개의 자식 노드를 가질 수 있는 트리

```java
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    
    public TreeNode(int val) {
        this.val = val;
        this.left = null;
        this.right = null;
    }
}
```

### 이진 탐색 트리(Binary Search Tree)

이진 트리의 일종으로, 다음 조건을 만족합니다:
- 왼쪽 서브트리의 모든 노드는 현재 노드보다 작은 값을 가짐
- 오른쪽 서브트리의 모든 노드는 현재 노드보다 큰 값을 가짐

```java
class BinarySearchTree {
    TreeNode root;
    
    public void insert(int val) {
        root = insertRec(root, val);
    }
    
    private TreeNode insertRec(TreeNode root, int val) {
        if (root == null) {
            root = new TreeNode(val);
            return root;
        }
        
        if (val < root.val)
            root.left = insertRec(root.left, val);
        else if (val > root.val)
            root.right = insertRec(root.right, val);
            
        return root;
    }
}
```

## 트리 순회(Tree Traversal)

### 깊이 우선 탐색(DFS, Depth-First Search)

DFS는 가능한 한 깊이 내려가면서 노드를 탐색하는 방법입니다. 이진 트리에서는 세 가지 방식으로 구현할 수 있습니다:

#### 1. 전위 순회(Preorder Traversal): 루트 → 왼쪽 → 오른쪽

```java
public void preorder(TreeNode node) {
    if (node == null) return;
    
    System.out.print(node.val + " "); // 현재 노드 방문
    preorder(node.left);              // 왼쪽 서브트리 순회
    preorder(node.right);             // 오른쪽 서브트리 순회
}
```

#### 2. 중위 순회(Inorder Traversal): 왼쪽 → 루트 → 오른쪽

```java
public void inorder(TreeNode node) {
    if (node == null) return;
    
    inorder(node.left);               // 왼쪽 서브트리 순회
    System.out.print(node.val + " "); // 현재 노드 방문
    inorder(node.right);              // 오른쪽 서브트리 순회
}
```

#### 3. 후위 순회(Postorder Traversal): 왼쪽 → 오른쪽 → 루트

```java
public void postorder(TreeNode node) {
    if (node == null) return;
    
    postorder(node.left);             // 왼쪽 서브트리 순회
    postorder(node.right);            // 오른쪽 서브트리 순회
    System.out.print(node.val + " "); // 현재 노드 방문
}
```

### 너비 우선 탐색(BFS, Breadth-First Search)

BFS는 같은 레벨에 있는 노드들을 먼저 탐색한 후 다음 레벨로 넘어가는 방법입니다. 큐(Queue)를 사용하여 구현합니다.

#### 레벨 순회(Level Order Traversal)

```java
public void levelOrder(TreeNode root) {
    if (root == null) return;
    
    Queue<TreeNode> queue = new LinkedList<>();
    queue.add(root);
    
    while (!queue.isEmpty()) {
        TreeNode current = queue.poll();
        System.out.print(current.val + " "); // 현재 노드 방문
        
        // 자식 노드들을 큐에 추가
        if (current.left != null) {
            queue.add(current.left);
        }
        if (current.right != null) {
            queue.add(current.right);
        }
    }
}
```

## 트리의 높이와 깊이 계산

### 트리의 높이 계산 (DFS 활용)

```java
public int height(TreeNode node) {
    if (node == null) return -1; // 빈 트리의 높이는 -1
    
    int leftHeight = height(node.left);
    int rightHeight = height(node.right);
    
    return Math.max(leftHeight, rightHeight) + 1;
}
```

### 노드의 깊이 계산 (DFS 활용)

```java
public int depth(TreeNode root, TreeNode node, int level) {
    if (root == null) return -1;
    if (root == node) return level;
    
    int leftDepth = depth(root.left, node, level + 1);
    if (leftDepth != -1) return leftDepth;
    
    return depth(root.right, node, level + 1);
}
```

## 완전한 트리 구현 예제

아래는 이진 트리를 구현하고 다양한 순회 방법과 속성을 계산하는 완전한 Java 예제입니다:

```java
import java.util.LinkedList;
import java.util.Queue;

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    
    public TreeNode(int val) {
        this.val = val;
        this.left = null;
        this.right = null;
    }
}

public class TreeExample {
    TreeNode root;
    
    // 트리 생성 예제
    public void createSampleTree() {
        root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.left.right = new TreeNode(5);
        root.right.left = new TreeNode(6);
        root.right.right = new TreeNode(7);
    }
    
    // DFS 순회 방법들
    public void preorder(TreeNode node) {
        if (node == null) return;
        System.out.print(node.val + " ");
        preorder(node.left);
        preorder(node.right);
    }
    
    public void inorder(TreeNode node) {
        if (node == null) return;
        inorder(node.left);
        System.out.print(node.val + " ");
        inorder(node.right);
    }
    
    public void postorder(TreeNode node) {
        if (node == null) return;
        postorder(node.left);
        postorder(node.right);
        System.out.print(node.val + " ");
    }
    
    // BFS 순회 방법
    public void levelOrder(TreeNode root) {
        if (root == null) return;
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        
        while (!queue.isEmpty()) {
            TreeNode current = queue.poll();
            System.out.print(current.val + " ");
            
            if (current.left != null) {
                queue.add(current.left);
            }
            if (current.right != null) {
                queue.add(current.right);
            }
        }
    }
    
    // 트리의 높이 계산
    public int height(TreeNode node) {
        if (node == null) return -1;
        
        int leftHeight = height(node.left);
        int rightHeight = height(node.right);
        
        return Math.max(leftHeight, rightHeight) + 1;
    }
    
    // 노드의 차수 계산
    public int degree(TreeNode node) {
        if (node == null) return 0;
        
        int count = 0;
        if (node.left != null) count++;
        if (node.right != null) count++;
        
        return count;
    }
    
    // 트리의 최대 차수 계산
    public int maxDegree(TreeNode node) {
        if (node == null) return 0;
        
        int currentDegree = degree(node);
        int leftMaxDegree = maxDegree(node.left);
        int rightMaxDegree = maxDegree(node.right);
        
        return Math.max(currentDegree, Math.max(leftMaxDegree, rightMaxDegree));
    }
    
    public static void main(String[] args) {
        TreeExample tree = new TreeExample();
        tree.createSampleTree();
        
        System.out.println("Preorder traversal:");
        tree.preorder(tree.root);
        System.out.println("\n\nInorder traversal:");
        tree.inorder(tree.root);
        System.out.println("\n\nPostorder traversal:");
        tree.postorder(tree.root);
        System.out.println("\n\nLevel order traversal:");
        tree.levelOrder(tree.root);
        
        System.out.println("\n\nHeight of tree: " + tree.height(tree.root));
        System.out.println("Maximum degree in tree: " + tree.maxDegree(tree.root));
    }
}
```

## 트리 응용 사례

1. **파일 시스템**: 디렉토리와 파일의 계층 구조
2. **HTML/XML DOM**: 웹 페이지의 구조
3. **데이터베이스 인덱싱**: B-트리, B+트리
4. **네트워크 라우팅**: 최단 경로 찾기
5. **의사결정 트리**: 머신러닝에서 분류 문제 해결
6. **게임 AI**: 미니맥스 알고리즘의 게임 트리

트리는 계층적 데이터를 표현하고 효율적으로 탐색하는 데 매우 유용한 자료구조입니다. 특히 이진 트리와 이진 탐색 트리는 많은 알고리즘과 응용 프로그램에서 핵심적인 역할을 합니다.