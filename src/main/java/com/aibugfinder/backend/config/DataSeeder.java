package com.aibugfinder.backend.config;

import com.aibugfinder.backend.entity.Difficulty;
import com.aibugfinder.backend.entity.Problem;
import com.aibugfinder.backend.entity.Track;
import com.aibugfinder.backend.repository.ProblemRepository;
import com.aibugfinder.backend.repository.TrackRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final TrackRepository trackRepository;
    private final ProblemRepository problemRepository;

    public DataSeeder(TrackRepository trackRepository, ProblemRepository problemRepository) {
        this.trackRepository = trackRepository;
        this.problemRepository = problemRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (TrackSeed trackSeed : catalog()) {
            Track track = trackRepository.findByNameIgnoreCase(trackSeed.name())
                    .orElseGet(Track::new);
            track.setName(trackSeed.name());
            track.setDescription(trackSeed.description());
            track.setDisplayOrder(trackSeed.order());
            Track savedTrack = trackRepository.save(track);

            int problemOrder = 1;
            for (ProblemSeed problemSeed : trackSeed.problems()) {
                Problem problem = problemRepository.findByTrackIdAndTitleIgnoreCase(savedTrack.getId(), problemSeed.title())
                        .orElseGet(Problem::new);
                problem.setTrack(savedTrack);
                problem.setTitle(problemSeed.title());
                problem.setDescription(description(problemSeed));
                problem.setDifficulty(problemSeed.difficulty());
                problem.setDisplayOrder(problemOrder++);
                problem.setStarterCode(starterCode(problemSeed));
                problem.setSolutionCode(solutionCode(problemSeed));
                problem.setTestCases(problemSeed.testCases());
                problemRepository.save(problem);
            }
        }
    }

    private List<TrackSeed> catalog() {
        return List.of(
                track(1, "Arrays", "Practice indexing, searching, two pointers, prefix sums, and in-place updates.", List.of(
                        p("Two Sum", Difficulty.EASY, "public int[] twoSum(int[] nums, int target)", "return new int[] {-1, -1};", "Use a hash map from value to index. For each number, check whether target - number was seen earlier.", tests("[2,7,11,15]\\n9", "[0,1]", "[3,2,4]\\n6", "[1,2]", "[3,3]\\n6", "[0,1]")),
                        p("Best Time to Buy and Sell Stock", Difficulty.EASY, "public int maxProfit(int[] prices)", "return 0;", "Track the lowest price seen so far and update the best profit at each day.", tests("[7,1,5,3,6,4]", "5", "[7,6,4,3,1]", "0", "[1,2]", "1")),
                        p("Contains Duplicate", Difficulty.EASY, "public boolean containsDuplicate(int[] nums)", "return false;", "Insert values into a set. If a value already exists, a duplicate was found.", tests("[1,2,3,1]", "true", "[1,2,3,4]", "false", "[1,1,1,3,3,4,3,2,4,2]", "true")),
                        p("Maximum Subarray", Difficulty.MEDIUM, "public int maxSubArray(int[] nums)", "return 0;", "Use Kadane's algorithm: keep the best subarray ending here and the best seen overall.", tests("[-2,1,-3,4,-1,2,1,-5,4]", "6", "[1]", "1", "[5,4,-1,7,8]", "23")),
                        p("Move Zeroes", Difficulty.EASY, "public int[] moveZeroes(int[] nums)", "return nums;", "Keep a write pointer for non-zero values, then fill the remaining positions with zeroes.", tests("[0,1,0,3,12]", "[1,3,12,0,0]", "[0]", "[0]", "[1,0,1]", "[1,1,0]")),
                        p("Rotate Array", Difficulty.MEDIUM, "public int[] rotate(int[] nums, int k)", "return nums;", "Reduce k modulo n, reverse the whole array, then reverse the two parts.", tests("[1,2,3,4,5,6,7]\\n3", "[5,6,7,1,2,3,4]", "[-1,-100,3,99]\\n2", "[3,99,-1,-100]", "[1]\\n0", "[1]")))),
                track(2, "Strings", "Master character counting, scanning, normalization, and string transformations.", List.of(
                        p("Valid Anagram", Difficulty.EASY, "public boolean isAnagram(String s, String t)", "return false;", "Count characters in one string and subtract counts using the other string.", tests("anagram\\nnagaram", "true", "rat\\ncar", "false", "a\\nab", "false")),
                        p("Valid Palindrome", Difficulty.EASY, "public boolean isPalindrome(String s)", "return false;", "Use two pointers, skip non-alphanumeric characters, and compare lowercase characters.", tests("A man, a plan, a canal: Panama", "true", "race a car", "false", " ", "true")),
                        p("First Unique Character", Difficulty.EASY, "public int firstUniqChar(String s)", "return -1;", "Count every character, then scan again to find the first count equal to one.", tests("leetcode", "0", "loveleetcode", "2", "aabb", "-1")),
                        p("Longest Common Prefix", Difficulty.EASY, "public String longestCommonPrefix(String[] strs)", "return \"\";", "Start with the first word as the prefix and shrink it until every word starts with it.", tests("[flower,flow,flight]", "fl", "[dog,racecar,car]", "", "[interview,internal,internet]", "inte")),
                        p("Reverse String", Difficulty.EASY, "public char[] reverseString(char[] s)", "return s;", "Swap characters from both ends while moving two pointers toward the middle.", tests("[h,e,l,l,o]", "[o,l,l,e,h]", "[H,a,n,n,a,h]", "[h,a,n,n,a,H]", "[a]", "[a]")),
                        p("String Compression", Difficulty.MEDIUM, "public int compress(char[] chars)", "return 0;", "Scan groups of equal characters, write the character, then write count digits when count is greater than one.", tests("[a,a,b,b,c,c,c]", "6", "[a]", "1", "[a,b,b,b,b,b,b,b,b,b,b,b,b]", "4")))),
                track(3, "Binary Search", "Learn sorted-search patterns, answer-space search, and boundary handling.", List.of(
                        p("Binary Search", Difficulty.EASY, "public int search(int[] nums, int target)", "return -1;", "Maintain left and right bounds. Compare target with nums[mid] and discard half of the range.", tests("[-1,0,3,5,9,12]\\n9", "4", "[-1,0,3,5,9,12]\\n2", "-1", "[5]\\n5", "0")),
                        p("Search Insert Position", Difficulty.EASY, "public int searchInsert(int[] nums, int target)", "return 0;", "Binary search for the first index whose value is greater than or equal to target.", tests("[1,3,5,6]\\n5", "2", "[1,3,5,6]\\n2", "1", "[1,3,5,6]\\n7", "4")),
                        p("First and Last Position", Difficulty.MEDIUM, "public int[] searchRange(int[] nums, int target)", "return new int[] {-1, -1};", "Run lower-bound twice: first for target, then for target + 1, and convert the second bound to the last index.", tests("[5,7,7,8,8,10]\\n8", "[3,4]", "[5,7,7,8,8,10]\\n6", "[-1,-1]", "[]\\n0", "[-1,-1]")),
                        p("Find Peak Element", Difficulty.MEDIUM, "public int findPeakElement(int[] nums)", "return 0;", "Compare nums[mid] with nums[mid + 1]. Move toward the side that must contain a peak.", tests("[1,2,3,1]", "2", "[1,2,1,3,5,6,4]", "5", "[1]", "0")),
                        p("Search in Rotated Sorted Array", Difficulty.MEDIUM, "public int search(int[] nums, int target)", "return -1;", "At each step, identify which half is sorted and decide whether the target lies inside that half.", tests("[4,5,6,7,0,1,2]\\n0", "4", "[4,5,6,7,0,1,2]\\n3", "-1", "[1]\\n0", "-1")),
                        p("Find Minimum in Rotated Sorted Array", Difficulty.MEDIUM, "public int findMin(int[] nums)", "return 0;", "Compare mid with right. If nums[mid] is greater, the minimum is to the right; otherwise it is at mid or left.", tests("[3,4,5,1,2]", "1", "[4,5,6,7,0,1,2]", "0", "[11,13,15,17]", "11")))),
                track(4, "Recursion & Backtracking", "Build recursive thinking with choices, base cases, and undo steps.", List.of(
                        p("Fibonacci Number", Difficulty.EASY, "public int fib(int n)", "return 0;", "Use recursion with memoization or an iterative DP to avoid repeated work.", tests("2", "1", "3", "2", "4", "3")),
                        p("Climbing Stairs", Difficulty.EASY, "public int climbStairs(int n)", "return 0;", "The number of ways for step n equals ways(n - 1) + ways(n - 2).", tests("2", "2", "3", "3", "5", "8")),
                        p("Subsets", Difficulty.MEDIUM, "public List<List<Integer>> subsets(int[] nums)", "return new ArrayList<>();", "Backtrack by choosing to include or skip each number.", tests("[1,2,3]", "[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]", "[0]", "[[],[0]]", "[]", "[[]]")),
                        p("Permutations", Difficulty.MEDIUM, "public List<List<Integer>> permute(int[] nums)", "return new ArrayList<>();", "Build a path, mark used numbers, and backtrack after each recursive call.", tests("[1,2,3]", "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]", "[0,1]", "[[0,1],[1,0]]", "[1]", "[[1]]")),
                        p("Generate Parentheses", Difficulty.MEDIUM, "public List<String> generateParenthesis(int n)", "return new ArrayList<>();", "Add '(' while open count is below n and ')' while close count is below open count.", tests("3", "[((())),(()()),(())(),()(()),()()()]", "1", "[()]", "2", "[(()),()()]")),
                        p("Combination Sum", Difficulty.MEDIUM, "public List<List<Integer>> combinationSum(int[] candidates, int target)", "return new ArrayList<>();", "Backtrack from a start index, reuse the same candidate when it remains valid, and stop when sum reaches target.", tests("[2,3,6,7]\\n7", "[[2,2,3],[7]]", "[2,3,5]\\n8", "[[2,2,2,2],[2,3,3],[3,5]]", "[2]\\n1", "[]")))),
                track(5, "Linked List", "Build confidence with pointers, node traversal, and list mutation.", List.of(
                        p("Reverse Linked List", Difficulty.EASY, "public ListNode reverseList(ListNode head)", "return head;", "Walk the list with previous, current, and next pointers, reversing one link at a time.", tests("[1,2,3,4,5]", "[5,4,3,2,1]", "[1,2]", "[2,1]", "[]", "[]")),
                        p("Middle of the Linked List", Difficulty.EASY, "public ListNode middleNode(ListNode head)", "return head;", "Move slow by one step and fast by two steps. Slow lands at the middle.", tests("[1,2,3,4,5]", "[3,4,5]", "[1,2,3,4,5,6]", "[4,5,6]", "[1]", "[1]")),
                        p("Merge Two Sorted Lists", Difficulty.EASY, "public ListNode mergeTwoLists(ListNode list1, ListNode list2)", "return list1;", "Use a dummy node and repeatedly attach the smaller current node.", tests("[1,2,4]\\n[1,3,4]", "[1,1,2,3,4,4]", "[]\\n[]", "[]", "[]\\n[0]", "[0]")),
                        p("Linked List Cycle", Difficulty.EASY, "public boolean hasCycle(ListNode head)", "return false;", "Use Floyd's slow and fast pointers. If they meet, a cycle exists.", tests("[3,2,0,-4]\\n1", "true", "[1,2]\\n0", "true", "[1]\\n-1", "false")),
                        p("Remove Nth Node From End", Difficulty.MEDIUM, "public ListNode removeNthFromEnd(ListNode head, int n)", "return head;", "Use a dummy node. Move fast n steps ahead, then move fast and slow together before deleting slow.next.", tests("[1,2,3,4,5]\\n2", "[1,2,3,5]", "[1]\\n1", "[]", "[1,2]\\n1", "[1]")),
                        p("Add Two Numbers", Difficulty.MEDIUM, "public ListNode addTwoNumbers(ListNode l1, ListNode l2)", "return l1;", "Traverse both lists with a carry, create one output digit per step, and continue while carry remains.", tests("[2,4,3]\\n[5,6,4]", "[7,0,8]", "[0]\\n[0]", "[0]", "[9,9,9,9,9,9,9]\\n[9,9,9,9]", "[8,9,9,9,0,0,0,1]")))),
                track(6, "Stacks", "Solve nested, monotonic, and last-in-first-out problems.", List.of(
                        p("Valid Parentheses", Difficulty.EASY, "public boolean isValid(String s)", "return false;", "Push opening brackets. For each closing bracket, pop and verify the matching opener.", tests("()", "true", "()[]{}", "true", "(]", "false")),
                        p("Min Stack", Difficulty.MEDIUM, "public List<Integer> minStack(String[] operations, int[] values)", "return new ArrayList<>();", "Keep one stack for values and another stack for the minimum after each push.", tests("[push,push,push,getMin,pop,top,getMin]\\n[-2,0,-3,0,0,0,0]", "[null,null,null,-3,null,0,-2]", "[push,getMin]\\n[5,0]", "[null,5]", "[push,push,getMin]\\n[2,1,0]", "[null,null,1]")),
                        p("Daily Temperatures", Difficulty.MEDIUM, "public int[] dailyTemperatures(int[] temperatures)", "return new int[temperatures.length];", "Use a decreasing stack of indexes. Pop indexes when a warmer temperature appears.", tests("[73,74,75,71,69,72,76,73]", "[1,1,4,2,1,1,0,0]", "[30,40,50,60]", "[1,1,1,0]", "[30,60,90]", "[1,1,0]")),
                        p("Evaluate Reverse Polish Notation", Difficulty.MEDIUM, "public int evalRPN(String[] tokens)", "return 0;", "Push numbers. When an operator appears, pop two values, apply the operator, and push the result.", tests("[2,1,+,3,*]", "9", "[4,13,5,/,+]", "6", "[10,6,9,3,+,-11,*,/,*,17,+,5,+]", "22")),
                        p("Next Greater Element I", Difficulty.EASY, "public int[] nextGreaterElement(int[] nums1, int[] nums2)", "return new int[nums1.length];", "Build next-greater values for nums2 with a monotonic stack, then answer nums1 by map lookup.", tests("[4,1,2]\\n[1,3,4,2]", "[-1,3,-1]", "[2,4]\\n[1,2,3,4]", "[3,-1]", "[1]\\n[1]", "[-1]")),
                        p("Simplify Path", Difficulty.MEDIUM, "public String simplifyPath(String path)", "return \"/\";", "Split by '/', ignore empty and '.', pop for '..', and join the remaining folders.", tests("/home/", "/home", "/../", "/", "/home//foo/", "/home/foo")))),
                track(7, "Queues", "Practice FIFO simulation, sliding windows, and queue-based processing.", List.of(
                        p("Implement Queue Using Stacks", Difficulty.EASY, "public List<Integer> queueUsingStacks(String[] operations, int[] values)", "return new ArrayList<>();", "Use an input stack for pushes and an output stack for pops/peeks. Move items only when output is empty.", tests("[push,push,peek,pop,empty]\\n[1,2,0,0,0]", "[null,null,1,1,false]", "[empty]", "[true]", "[push,pop,empty]\\n[1,0,0]", "[null,1,true]")),
                        p("Number of Recent Calls", Difficulty.EASY, "public int[] recentCounter(int[] calls)", "return new int[calls.length];", "Keep a queue of timestamps within the last 3000 milliseconds and remove older calls.", tests("[1,100,3001,3002]", "[1,2,3,3]", "[642,1849,4921,5936,5957]", "[1,2,1,2,3]", "[1]", "[1]")),
                        p("Moving Average From Data Stream", Difficulty.EASY, "public double[] movingAverage(int size, int[] values)", "return new double[values.length];", "Maintain a queue and running sum of the latest size values.", tests("3\\n[1,10,3,5]", "[1.0,5.5,4.66667,6.0]", "1\\n[5,6]", "[5.0,6.0]", "2\\n[1,2,3]", "[1.0,1.5,2.5]")),
                        p("Time Needed to Buy Tickets", Difficulty.EASY, "public int timeRequiredToBuy(int[] tickets, int k)", "return 0;", "Simulate turns or sum min(tickets[i], tickets[k]) with one less for people after k.", tests("[2,3,2]\\n2", "6", "[5,1,1,1]\\n0", "8", "[1]\\n0", "1")),
                        p("Reveal Cards In Increasing Order", Difficulty.MEDIUM, "public int[] deckRevealedIncreasing(int[] deck)", "return deck;", "Sort the deck and use a queue of indexes to place each card in reveal order.", tests("[17,13,11,2,3,5,7]", "[2,13,3,11,5,17,7]", "[1,1000]", "[1,1000]", "[1]", "[1]")),
                        p("First Non-Repeating Character Stream", Difficulty.MEDIUM, "public String firstNonRepeating(String stream)", "return \"\";", "Count characters and keep candidates in a queue. Remove from the front while count is greater than one.", tests("aabc", "a#bb", "zz", "z#", "abcabc", "aaabc#")))),
                track(8, "Binary Trees", "Traverse trees with DFS, BFS, depth, and path reasoning.", List.of(
                        p("Maximum Depth of Binary Tree", Difficulty.EASY, "public int maxDepth(TreeNode root)", "return 0;", "Return 0 for null. Otherwise return 1 plus the max depth of left and right.", tests("[3,9,20,null,null,15,7]", "3", "[1,null,2]", "2", "[]", "0")),
                        p("Invert Binary Tree", Difficulty.EASY, "public TreeNode invertTree(TreeNode root)", "return root;", "Recursively swap left and right children for every node.", tests("[4,2,7,1,3,6,9]", "[4,7,2,9,6,3,1]", "[2,1,3]", "[2,3,1]", "[]", "[]")),
                        p("Same Tree", Difficulty.EASY, "public boolean isSameTree(TreeNode p, TreeNode q)", "return false;", "Two trees are the same if both roots match and both left and right subtrees are the same.", tests("[1,2,3]\\n[1,2,3]", "true", "[1,2]\\n[1,null,2]", "false", "[1,2,1]\\n[1,1,2]", "false")),
                        p("Binary Tree Level Order Traversal", Difficulty.MEDIUM, "public List<List<Integer>> levelOrder(TreeNode root)", "return new ArrayList<>();", "Use a queue. Process exactly the current queue size for each level.", tests("[3,9,20,null,null,15,7]", "[[3],[9,20],[15,7]]", "[1]", "[[1]]", "[]", "[]")),
                        p("Path Sum", Difficulty.EASY, "public boolean hasPathSum(TreeNode root, int targetSum)", "return false;", "Subtract node values along the path and check target at leaf nodes.", tests("[5,4,8,11,null,13,4,7,2,null,null,null,1]\\n22", "true", "[1,2,3]\\n5", "false", "[]\\n0", "false")),
                        p("Lowest Common Ancestor of Binary Tree", Difficulty.MEDIUM, "public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q)", "return root;", "If root is null, p, or q, return root. Search both sides; if both return non-null, root is the LCA.", tests("[3,5,1,6,2,0,8,null,null,7,4]\\n5\\n1", "3", "[3,5,1,6,2,0,8,null,null,7,4]\\n5\\n4", "5", "[1,2]\\n1\\n2", "1")))),
                track(9, "BST", "Use binary-search-tree ordering to search, validate, and transform trees.", List.of(
                        p("Search in a BST", Difficulty.EASY, "public TreeNode searchBST(TreeNode root, int val)", "return null;", "Move left when val is smaller than root, right when larger, and stop when found or null.", tests("[4,2,7,1,3]\\n2", "[2,1,3]", "[4,2,7,1,3]\\n5", "[]", "[1]\\n1", "[1]")),
                        p("Validate Binary Search Tree", Difficulty.MEDIUM, "public boolean isValidBST(TreeNode root)", "return false;", "Carry lower and upper bounds down the tree and require every node to stay inside its range.", tests("[2,1,3]", "true", "[5,1,4,null,null,3,6]", "false", "[2,2,2]", "false")),
                        p("Insert into a BST", Difficulty.MEDIUM, "public TreeNode insertIntoBST(TreeNode root, int val)", "return root;", "Walk according to BST order until a null child is found, then attach the new node.", tests("[4,2,7,1,3]\\n5", "[4,2,7,1,3,5]", "[40,20,60,10,30,50,70]\\n25", "[40,20,60,10,30,50,70,null,null,25]", "[]\\n5", "[5]")),
                        p("Kth Smallest Element in a BST", Difficulty.MEDIUM, "public int kthSmallest(TreeNode root, int k)", "return 0;", "Inorder traversal of a BST is sorted. Stop when the kth value is visited.", tests("[3,1,4,null,2]\\n1", "1", "[5,3,6,2,4,null,null,1]\\n3", "3", "[1]\\n1", "1")),
                        p("Minimum Absolute Difference in BST", Difficulty.EASY, "public int getMinimumDifference(TreeNode root)", "return 0;", "Inorder traversal gives sorted values. Track the previous value and smallest adjacent difference.", tests("[4,2,6,1,3]", "1", "[1,0,48,null,null,12,49]", "1", "[2,1]", "1")),
                        p("Convert Sorted Array to BST", Difficulty.EASY, "public TreeNode sortedArrayToBST(int[] nums)", "return null;", "Choose the middle element as root, recursively build the left and right halves.", tests("[-10,-3,0,5,9]", "[0,-3,9,-10,null,5]", "[1,3]", "[3,1]", "[]", "[]")))),
                track(10, "Heaps", "Use priority queues for top-k, merging, scheduling, and streaming problems.", List.of(
                        p("Kth Largest Element in an Array", Difficulty.MEDIUM, "public int findKthLargest(int[] nums, int k)", "return 0;", "Keep a min-heap of size k. The heap top is the kth largest value.", tests("[3,2,1,5,6,4]\\n2", "5", "[3,2,3,1,2,4,5,5,6]\\n4", "4", "[1]\\n1", "1")),
                        p("Top K Frequent Elements", Difficulty.MEDIUM, "public int[] topKFrequent(int[] nums, int k)", "return new int[0];", "Count frequencies, then use a heap or bucket list to extract the k most frequent values.", tests("[1,1,1,2,2,3]\\n2", "[1,2]", "[1]\\n1", "[1]", "[4,4,4,6,6,7]\\n2", "[4,6]")),
                        p("Merge K Sorted Lists", Difficulty.HARD, "public ListNode mergeKLists(ListNode[] lists)", "return null;", "Push each list head into a min-heap by node value, then repeatedly append the smallest node.", tests("[[1,4,5],[1,3,4],[2,6]]", "[1,1,2,3,4,4,5,6]", "[]", "[]", "[[]]", "[]")),
                        p("Last Stone Weight", Difficulty.EASY, "public int lastStoneWeight(int[] stones)", "return 0;", "Use a max-heap. Smash the two largest stones and push the difference when non-zero.", tests("[2,7,4,1,8,1]", "1", "[1]", "1", "[3,3]", "0")),
                        p("Find Median from Data Stream", Difficulty.HARD, "public double[] medianSlidingStream(int[] nums)", "return new double[nums.length];", "Keep a max-heap for the lower half and a min-heap for the upper half, rebalancing after each insertion.", tests("[1,2,3]", "[1.0,1.5,2.0]", "[5,15,1,3]", "[5.0,10.0,5.0,4.0]", "[2]", "[2.0]")),
                        p("Task Scheduler", Difficulty.MEDIUM, "public int leastInterval(char[] tasks, int n)", "return 0;", "Count task frequencies and use the idle-slot formula or a max-heap simulation.", tests("[A,A,A,B,B,B]\\n2", "8", "[A,C,A,B,D,B]\\n1", "6", "[A,A,A,B,B,B]\\n3", "10")))),
                track(11, "Tries", "Store prefixes efficiently for search, autocomplete, and word grids.", List.of(
                        p("Implement Trie", Difficulty.MEDIUM, "public List<Boolean> trieOps(String[] operations, String[] words)", "return new ArrayList<>();", "Each node stores children and an isWord flag. Insert and traverse one character at a time.", tests("[insert,search,startsWith]\\n[apple,apple,app]", "[null,true,true]", "[insert,search]\\n[apple,app]", "[null,false]", "[insert,startsWith]\\n[app,ap]", "[null,true]")),
                        p("Word Search II", Difficulty.HARD, "public List<String> findWords(char[][] board, String[] words)", "return new ArrayList<>();", "Build a trie of words, then DFS from every board cell while pruning missing prefixes.", tests("[[o,a,a,n],[e,t,a,e],[i,h,k,r],[i,f,l,v]]\\n[oath,pea,eat,rain]", "[oath,eat]", "[[a,b],[c,d]]\\n[abcb]", "[]", "[[a]]\\n[a]", "[a]")),
                        p("Replace Words", Difficulty.MEDIUM, "public String replaceWords(List<String> dictionary, String sentence)", "return sentence;", "Put roots in a trie and replace each word with the shortest root found while scanning characters.", tests("[cat,bat,rat]\\nthe cattle was rattled by the battery", "the cat was rat by the bat", "[a,b,c]\\naadsfasf absbs bbab cadsfafs", "a a b c", "[a]\\na aa aaa", "a a a")),
                        p("Longest Word in Dictionary", Difficulty.MEDIUM, "public String longestWord(String[] words)", "return \"\";", "Sort words or use a trie, only accepting a word when every prefix is also a word.", tests("[w,wo,wor,worl,world]", "world", "[a,banana,app,appl,ap,apply,apple]", "apple", "[k,ki,kir,kira,kiran]", "kiran")),
                        p("Map Sum Pairs", Difficulty.MEDIUM, "public int[] mapSum(String[] operations, String[] keys, int[] values)", "return new int[0];", "Store key values and update prefix sums in trie nodes by the delta when a key changes.", tests("[insert,sum,insert,sum]\\n[apple,ap,app,ap]\\n[3,0,2,0]", "[null,3,null,5]", "[insert,insert,sum]\\n[a,a,aa]\\n[3,2,0]", "[null,null,2]", "[insert,sum]\\n[abc,ab]\\n[5,0]", "[null,5]")),
                        p("Search Suggestions System", Difficulty.MEDIUM, "public List<List<String>> suggestedProducts(String[] products, String searchWord)", "return new ArrayList<>();", "Sort products and for each prefix collect up to three lexicographically smallest matches.", tests("[mobile,mouse,moneypot,monitor,mousepad]\\nmouse", "[[mobile,moneypot,monitor],[mobile,moneypot,monitor],[mouse,mousepad],[mouse,mousepad],[mouse,mousepad]]", "[havana]\\nhavana", "[[havana],[havana],[havana],[havana],[havana],[havana]]", "[bags,baggage,banner,box,cloths]\\nbags", "[[baggage,bags,banner],[baggage,bags,banner],[baggage,bags],[bags]]")))),
                track(12, "Graphs", "Traverse nodes and edges with BFS, DFS, topological sort, and shortest paths.", List.of(
                        p("Number of Islands", Difficulty.MEDIUM, "public int numIslands(char[][] grid)", "return 0;", "When land is found, count one island and DFS/BFS to mark all connected land as visited.", tests("[[1,1,1,1,0],[1,1,0,1,0],[1,1,0,0,0],[0,0,0,0,0]]", "1", "[[1,1,0,0,0],[1,1,0,0,0],[0,0,1,0,0],[0,0,0,1,1]]", "3", "[[0]]", "0")),
                        p("Clone Graph", Difficulty.MEDIUM, "public Node cloneGraph(Node node)", "return node;", "Use a map from original node to cloned node and DFS/BFS through neighbors.", tests("[[2,4],[1,3],[2,4],[1,3]]", "[[2,4],[1,3],[2,4],[1,3]]", "[]", "[]", "[[]]", "[[]]")),
                        p("Course Schedule", Difficulty.MEDIUM, "public boolean canFinish(int numCourses, int[][] prerequisites)", "return false;", "Build indegrees and adjacency, then run Kahn's topological sort to detect whether all courses can be taken.", tests("2\\n[[1,0]]", "true", "2\\n[[1,0],[0,1]]", "false", "3\\n[[1,0],[2,1]]", "true")),
                        p("Rotting Oranges", Difficulty.MEDIUM, "public int orangesRotting(int[][] grid)", "return -1;", "Start BFS from all rotten oranges at once and count minutes by layers.", tests("[[2,1,1],[1,1,0],[0,1,1]]", "4", "[[2,1,1],[0,1,1],[1,0,1]]", "-1", "[[0,2]]", "0")),
                        p("Pacific Atlantic Water Flow", Difficulty.MEDIUM, "public List<List<Integer>> pacificAtlantic(int[][] heights)", "return new ArrayList<>();", "Reverse the flow: DFS/BFS from Pacific edges and Atlantic edges, then intersect reachable cells.", tests("[[1,2,2,3,5],[3,2,3,4,4],[2,4,5,3,1],[6,7,1,4,5],[5,1,1,2,4]]", "[[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]", "[[1]]", "[[0,0]]", "[[2,1],[1,2]]", "[[0,0],[0,1],[1,0],[1,1]]")),
                        p("Network Delay Time", Difficulty.MEDIUM, "public int networkDelayTime(int[][] times, int n, int k)", "return -1;", "Run Dijkstra from k using an adjacency list and min-heap, then take the maximum shortest distance.", tests("[[2,1,1],[2,3,1],[3,4,1]]\\n4\\n2", "2", "[[1,2,1]]\\n2\\n1", "1", "[[1,2,1]]\\n2\\n2", "-1")))),
                track(13, "DP", "Break problems into reusable states and transitions.", List.of(
                        p("House Robber", Difficulty.MEDIUM, "public int rob(int[] nums)", "return 0;", "For each house, decide between robbing it plus dp[i - 2] or skipping it with dp[i - 1].", tests("[1,2,3,1]", "4", "[2,7,9,3,1]", "12", "[2,1,1,2]", "4")),
                        p("Coin Change", Difficulty.MEDIUM, "public int coinChange(int[] coins, int amount)", "return -1;", "Let dp[x] be the fewest coins for amount x. Try every coin for every amount.", tests("[1,2,5]\\n11", "3", "[2]\\n3", "-1", "[1]\\n0", "0")),
                        p("Longest Increasing Subsequence", Difficulty.MEDIUM, "public int lengthOfLIS(int[] nums)", "return 0;", "Maintain tails where tails[i] is the smallest possible tail for an increasing subsequence of length i + 1.", tests("[10,9,2,5,3,7,101,18]", "4", "[0,1,0,3,2,3]", "4", "[7,7,7,7,7,7,7]", "1")),
                        p("Unique Paths", Difficulty.MEDIUM, "public int uniquePaths(int m, int n)", "return 0;", "Each cell can be reached from top or left, so dp[row][col] = top + left.", tests("3\\n7", "28", "3\\n2", "3", "1\\n1", "1")),
                        p("Longest Common Subsequence", Difficulty.MEDIUM, "public int longestCommonSubsequence(String text1, String text2)", "return 0;", "Use a 2D table where matching characters extend a subsequence and mismatches take the best neighbor.", tests("abcde\\nace", "3", "abc\\nabc", "3", "abc\\ndef", "0")),
                        p("Word Break", Difficulty.MEDIUM, "public boolean wordBreak(String s, List<String> wordDict)", "return false;", "Let dp[i] mean s[0..i) can be segmented. Try dictionary words ending at each index.", tests("leetcode\\n[leet,code]", "true", "applepenapple\\n[apple,pen]", "true", "catsandog\\n[cats,dog,sand,and,cat]", "false")))),
                track(14, "Greedy", "Make locally optimal choices and prove they lead to a global answer.", List.of(
                        p("Jump Game", Difficulty.MEDIUM, "public boolean canJump(int[] nums)", "return false;", "Track the farthest reachable index. If the current index exceeds it, you are stuck.", tests("[2,3,1,1,4]", "true", "[3,2,1,0,4]", "false", "[0]", "true")),
                        p("Jump Game II", Difficulty.MEDIUM, "public int jump(int[] nums)", "return 0;", "Expand the current jump range and start a new jump when the current range ends.", tests("[2,3,1,1,4]", "2", "[2,3,0,1,4]", "2", "[0]", "0")),
                        p("Gas Station", Difficulty.MEDIUM, "public int canCompleteCircuit(int[] gas, int[] cost)", "return -1;", "If total gas is enough, reset the candidate start whenever the running tank goes negative.", tests("[1,2,3,4,5]\\n[3,4,5,1,2]", "3", "[2,3,4]\\n[3,4,3]", "-1", "[5]\\n[4]", "0")),
                        p("Assign Cookies", Difficulty.EASY, "public int findContentChildren(int[] g, int[] s)", "return 0;", "Sort greed factors and cookies, then give each child the smallest cookie that satisfies them.", tests("[1,2,3]\\n[1,1]", "1", "[1,2]\\n[1,2,3]", "2", "[10,9,8,7]\\n[5,6,7,8]", "2")),
                        p("Non-overlapping Intervals", Difficulty.MEDIUM, "public int eraseOverlapIntervals(int[][] intervals)", "return 0;", "Sort by end time and keep intervals whose start is after the previous kept end.", tests("[[1,2],[2,3],[3,4],[1,3]]", "1", "[[1,2],[1,2],[1,2]]", "2", "[[1,2],[2,3]]", "0")),
                        p("Partition Labels", Difficulty.MEDIUM, "public List<Integer> partitionLabels(String s)", "return new ArrayList<>();", "Record each character's last index, expand the current partition end, and cut when the scan reaches that end.", tests("ababcbacadefegdehijhklij", "[9,7,8]", "eccbbbbdec", "[10]", "abc", "[1,1,1]")))),
                track(15, "Miscellaneous", "Sharpen common interview patterns that do not fit one bucket.", List.of(
                        p("LRU Cache", Difficulty.MEDIUM, "public List<Integer> lruCache(String[] operations, int[][] values)", "return new ArrayList<>();", "Combine a hash map with a doubly linked list, or use an access-order LinkedHashMap.", tests("[LRUCache,put,put,get,put,get,put,get,get,get]\\n[[2],[1,1],[2,2],[1],[3,3],[2],[4,4],[1],[3],[4]]", "[null,null,null,1,null,-1,null,-1,3,4]", "[LRUCache,put,get]\\n[[1],[2,1],[2]]", "[null,null,1]", "[LRUCache,get]\\n[[2],[1]]", "[null,-1]")),
                        p("Encode and Decode Strings", Difficulty.MEDIUM, "public String[] encodeDecode(String[] strs)", "return strs;", "Encode each string as length + delimiter + content so any character can be safely decoded.", tests("[lint,code,love,you]", "[lint,code,love,you]", "[]", "[]", "[,abc]", "[,abc]")),
                        p("Product of Array Except Self", Difficulty.MEDIUM, "public int[] productExceptSelf(int[] nums)", "return new int[nums.length];", "Write prefix products left to right, then multiply by suffix products right to left.", tests("[1,2,3,4]", "[24,12,8,6]", "[-1,1,0,-3,3]", "[0,0,9,0,0]", "[2,3]", "[3,2]")),
                        p("Spiral Matrix", Difficulty.MEDIUM, "public List<Integer> spiralOrder(int[][] matrix)", "return new ArrayList<>();", "Maintain top, bottom, left, and right boundaries while walking the matrix edges inward.", tests("[[1,2,3],[4,5,6],[7,8,9]]", "[1,2,3,6,9,8,7,4,5]", "[[1,2,3,4],[5,6,7,8],[9,10,11,12]]", "[1,2,3,4,8,12,11,10,9,5,6,7]", "[[1]]", "[1]")),
                        p("Set Matrix Zeroes", Difficulty.MEDIUM, "public int[][] setZeroes(int[][] matrix)", "return matrix;", "Use first row and first column as markers, while separately remembering whether they originally contained zero.", tests("[[1,1,1],[1,0,1],[1,1,1]]", "[[1,0,1],[0,0,0],[1,0,1]]", "[[0,1,2,0],[3,4,5,2],[1,3,1,5]]", "[[0,0,0,0],[0,4,5,0],[0,3,1,0]]", "[[1]]", "[[1]]")),
                        p("Find the Duplicate Number", Difficulty.MEDIUM, "public int findDuplicate(int[] nums)", "return -1;", "Treat values as next pointers and use Floyd's cycle detection to find the repeated number.", tests("[1,3,4,2,2]", "2", "[3,1,3,4,2]", "3", "[3,3,3,3,3]", "3")))));
    }

    private TrackSeed track(int order, String name, String description, List<ProblemSeed> problems) {
        return new TrackSeed(order, name, description, problems);
    }

    private ProblemSeed p(String title,
            Difficulty difficulty,
            String signature,
            String defaultReturn,
            String approach,
            String testCases) {
        return new ProblemSeed(title, difficulty, signature, defaultReturn, approach, testCases);
    }

    private String tests(String input1, String output1, String input2, String output2, String input3, String output3) {
        return """
                [
                  {"input":"%s","expectedOutput":"%s"},
                  {"input":"%s","expectedOutput":"%s"},
                  {"input":"%s","expectedOutput":"%s"}
                ]
                """.formatted(
                escape(input1),
                escape(output1),
                escape(input2),
                escape(output2),
                escape(input3),
                escape(output3));
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String description(ProblemSeed problem) {
        return """
                %s

                Task:
                %s

                Return the required value exactly as shown in the examples.
                """.formatted(problem.title(), problem.approach());
    }

    private String starterCode(ProblemSeed problem) {
        return """
                import java.util.*;

                class Solution {
                    %s {
                        %s
                    }
                }
                """.formatted(problem.signature(), problem.defaultReturn());
    }

    private String solutionCode(ProblemSeed problem) {
        return """
                import java.util.*;

                class Solution {
                    /*
                     * Expected approach:
                     * %s
                     */
                    %s {
                        %s
                    }
                }
                """.formatted(problem.approach(), problem.signature(), problem.defaultReturn());
    }

    private record TrackSeed(int order, String name, String description, List<ProblemSeed> problems) {
    }

    private record ProblemSeed(String title,
            Difficulty difficulty,
            String signature,
            String defaultReturn,
            String approach,
            String testCases) {
    }
}
