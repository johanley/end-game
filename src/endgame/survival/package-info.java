/** 
 Probability of living for N more years (lx).
 
 <P>The core data I'm using is termed "Number of survivors at age x (lx)".
 The idea is that you start with a cohort of 100,000 people at birth (age 0).
 Then the lx table simply lists the number of survivors at each age.
 
 <P>With that data, you can calculate the answer to questions of this sort:  
 "if a Canadian man is of age 64, what is the probability he reaches 64 + N?"
 
 <P>The answer is simply taken as the ratio of two entries in the lx table, 
 one for 64 (denominator) and one for 64 + N (numerator).
 
 With such a table, one implements an iterative calculation like so, with each year in the simulation:
  <pre>
    my age is 64
    what is the probability p that I will survive to age 65? l(64, 65)
      generate a random number r between 0 and 1
      if r is greater or equal to p then 
         I die this year; the history ends
      else 
         I survive this year, and the history continues     
  </pre>
  That is, for each simulated year, you roll the dice (on Dec 31) and see if you survive or not.
  It's not an "up front" calculation, but an iterative and probabilistic one.

  <hr>  
  
  <P>The statcan data can be specific to province, but I'm not retaining that level of detail here.
  For the moment, the data will be for Canada-wide averages, one table for males and one for females.
  
   <p>The core data is updated by statcan every few years. 
   This tool should attempt to stay up to date with those tables.
   The current core reference is here : https://www150.statcan.gc.ca/n1/pub/84-537-x/84-537-x2021001-eng.htm
   
   <P>Statcan has a 1-year version and a 3-year version of this data.
   I'm not sure what exactly that means, because the tables in both versions have 1-year intervals.
   Perhaps the 3-year version has some kind of smoothing applied (3-year running average?); it's not clear from the documentation.
   Its stated that the 3-year version is more robust, so I'm going with that.
 
  <p>What I've selected is the 3-year data, AND taking the column with the most-recent 3-year period.
*/
package endgame.survival;