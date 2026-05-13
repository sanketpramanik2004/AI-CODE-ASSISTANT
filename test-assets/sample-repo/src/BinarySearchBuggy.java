public class BinarySearchBuggy {
    public int search(int[] nums, int target) {
        int left = 0;
        int right = nums.length;

        while (left < right) {
            int mid = (left + right) / 2;

            if (nums[mid] == target) {
                return mid;
            }

            if (nums[mid] < target) {
                left = mid;
            } else {
                right = mid - 1;
            }
        }

        return -1;
    }

    public boolean containsDuplicate(String[] names) {
        for (int i = 0; i < names.length; i++) {
            for (int j = i + 1; j < names.length; j++) {
                if (names[i] == names[j]) {
                    return true;
                }
            }
        }
        return false;
    }
}
