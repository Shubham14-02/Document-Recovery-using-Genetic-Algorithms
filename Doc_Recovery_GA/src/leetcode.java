import java.util.ArrayList;
import java.util.Arrays;

public class leetcode {

    public leetcode(){
        int[] arr = {1,2,3,4};
        twoSum(arr,7);
    }
    public int[] twoSum(int[] nums, int target) {
        ArrayList<Integer> res = new ArrayList<>();

        for(int i=0;i<nums.length;i++){
            for(int j=i+1;j<nums.length;j++){
                if (nums[i]+nums[j]==target){
                    res.add(i);
                    res.add(j);
                }
            }
        }
        int[] result = res.stream().mapToInt(i->i).toArray();
        System.out.println(Arrays.toString(result));

        return result;
    }

    public static void main(String[] args) {
        new leetcode();
    }
}