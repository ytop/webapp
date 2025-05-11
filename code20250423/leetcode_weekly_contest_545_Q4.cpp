/** 
weekly contest 545 


Q4. Count Numbers with Non-Decreasing Digits 

You are given two integers, l and r, represented as strings, and an integer b. Return the count of integers in the inclusive range [l, r] whose digits are in non-decreasing order when represented in base b.

An integer is considered to have non-decreasing digits if, when read from left to right (from the most significant digit to the least significant digit), each digit is greater than or equal to the previous one.

Since the answer may be too large, return it modulo 1e9 + 7.
*/
#include <bits/stdc++.h>
using namespace std;
typedef long long ll;

const int MOD = 1e9 + 7;

// Function to convert a character digit in base b to integer
int charToInt(char c, int b) {
    if (c >= '0' && c <= '9') return c - '0';
    else return 10 + (c - 'A');
}

// Function to count numbers with non-decreasing digits up to a given string s in base b
ll countUpTo(string s, int b) {
    int n = s.size();
    vector<vector<vector<ll>>> dp(n + 1, vector<vector<ll>>(2, vector<ll>(b, 0)));
    // dp[pos][tight][prev_digit]
    // pos: current position in the string
    // tight: whether the number formed so far is equal to the prefix of s (1) or less (0)
    // prev_digit: the previous digit used (0 to b-1)

    // Initialize for the first digit
    for (int d = 0; d < b; d++) {
        int val = charToInt(s[0], b);
        if (d < val) {
            dp[1][0][d] = 1;
        } else if (d == val) {
            dp[1][1][d] = 1;
        }
    }

    // Fill the DP table
    for (int pos = 1; pos < n; pos++) {
        int val = charToInt(s[pos], b);
        for (int tight = 0; tight < 2; tight++) {
            for (int prev_d = 0; prev_d < b; prev_d++) {
                if (dp[pos][tight][prev_d] == 0) continue;
                int up = (tight ? val : b - 1);
                for (int d = prev_d; d <= up; d++) {
                    int new_tight = tight && (d == val);
                    dp[pos + 1][new_tight][d] = (dp[pos + 1][new_tight][d] + dp[pos][tight][prev_d]) % MOD;
                }
            }
        }
    }

    // Sum up all possibilities at the last position
    ll res = 0;
    for (int tight = 0; tight < 2; tight++) {
        for (int d = 0; d < b; d++) {
            res = (res + dp[n][tight][d]) % MOD;
        }
    }
    return res;
}

// Function to count numbers with non-decreasing digits in range [l, r] in base b
ll countInRange(string l, string r, int b) {
    // Count numbers up to r
    ll countR = countUpTo(r, b);
    // Count numbers up to l-1
    ll countL = countUpTo(l, b);
    // Adjust for l itself if it has non-decreasing digits
    bool isNonDecreasing = true;
    for (int i = 1; i < l.size(); i++) {
        if (charToInt(l[i], b) < charToInt(l[i-1], b)) {
            isNonDecreasing = false;
            break;
        }
    }
    if (isNonDecreasing) {
        countL = (countL - 1 + MOD) % MOD; // Subtract 1 for l itself
    }
    // Result is countR - countL
    ll result = (countR - countL + MOD) % MOD;
    return result;
}

int main() {
    string l, r;
    int b;
    cin >> l >> r >> b;
    cout << countInRange(l, r, b) << endl;
    return 0;
}
