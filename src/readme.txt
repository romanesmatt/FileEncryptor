CYBR 372
Assignment One Design Analysis
Matt Romanes
300492211

Part One
For the first part of the assignment, I decided that the original implementation made no sense, especially given that our implementation has to work off the command line. 
So for my program to suit the requirements of the assignment, I decided to 'split' the assignment in
half. This way I could focus on extending the encryption and decryption processes separately.
In the main method I re-implemented it in such that it takes the arguments specified by the user as an array,
and depending on what the user inputs (i.e. "enc" or "dec") the program will execute the respective process.

Part Two
For the second part of the assignment, I decided to retain much of the original implementation
from Part One, with the key difference of course being that the IV was no longer being encrypted, but instead
being written directly onto the given file. For the problem at hand, I did have a choice as to whether to keep the IV secret or not,
as well as whether to encrypt the IV or not. As the IV was no longer needed to be specified, I decided to write it directly onto the file.
The main reason behind this is that the security requirements of an IV are not to a higher standard like that of the key, so having
access to this information will not be beneficial to potential attackers. That said, it was important to ensure security measures,
and therefore I decided that whenever encryption occurs, the IV changes to a random and unique one.

Part Three
For the third part of the assignment, the encryption and decryption processes was to be executed using a password.
This meant implementing safe security practices when playing with such sensitive information. For example, while the
password the user specifies is received by the program as a String, I immediately convert this to a char[] array at the
first opportunity. The main reason being that String are immutable, meaning that it wil be available in memory until a Garbage collector
clears it. In addition, any user that can access the memory dump can find the password in clear text which is undesirable
in terms of security. The benefit of a char[] array is that the data can be explicitly wiped, meaning that the data can be overwritten
(or just set to null) and the password will no longer be present anywhere in the system.

In addition to the implementation of security practices, I also implemented a random password generator should the user
fail to specify the password in the command line. However, in most cases the user must specify the password, which will create a key
from said password.

Part Four