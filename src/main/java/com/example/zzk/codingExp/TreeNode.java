package com.example.zzk.codingExp;

import java.util.LinkedList;
import java.util.Queue;

public class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int val) { this.val = val; }

    /**
     * 精简版：数组转二叉树
     */
    public static TreeNode   buildTree(Integer[] array) {
        if (array == null || array.length == 0 || array[0] == null) {
            return null;
        }

        TreeNode root = new TreeNode(array[0]);
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        int index = 1;

        while (!queue.isEmpty() && index < array.length) {
            TreeNode current = queue.poll();

            // 处理左子节点
            if (index < array.length && array[index] != null) {
                current.left = new TreeNode(array[index]);
                queue.offer(current.left);
            }
            index++;

            // 处理右子节点
            if (index < array.length && array[index] != null) {
                current.right = new TreeNode(array[index]);
                queue.offer(current.right);
            }
            index++;
        }

        return root;
    }

    /**
     * 前序遍历（用于验证结果）
     */
    public static void preOrder(TreeNode root) {
        if (root == null) return;
        System.out.print(root.val + " ");
        preOrder(root.left);
        preOrder(root.right);
    }

    /**
     * 层序遍历（用于验证结果）
     */
    public static void levelOrder(TreeNode root) {
        if (root == null) return;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode current = queue.poll();
            System.out.print(current.val + " ");

            if (current.left != null) queue.offer(current.left);
            if (current.right != null) queue.offer(current.right);
        }
    }

    public static void main(String[] args) {
        // 测试用例
        Integer[] array = {3,9,20,null,null,15,7};
        TreeNode root = buildTree(array);

        System.out.print("前序遍历: ");
        preOrder(root); // 输出: 1 2 3

        System.out.print("\n层序遍历: ");
        levelOrder(root); // 输出: 1 2 3
    }
}