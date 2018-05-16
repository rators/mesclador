 # Mesclador Job Coin Mixer
 
 ## Use
 1. Provide a list of new, unused addresses that you own to the mixer;
 2. The mixer provides a new deposit address that it owns;
 3. Transfer your jobcoins to that address in the [Jobcoin network](https://jobcoin.gemini.com/polyester);
 4. The mixer will detect a transfer by watching or polling the P2P [Jobcoin network](https://jobcoin.gemini.com/polyester);
 5. The mixer will transfer jobcoins from the deposit address into a series of “house accounts” along with all the other jobcoin currently being mixed; and
 6. Then, over some time the mixer will use the house account to dole out your jobcoins in smaller increments to the withdrawal addresses that were provided in step #1.
 
 ***Running the CLI App***:
  
 ```
 sbt> package
 sbt> run <registry_json_file> <kafka_config_file>
 mesclador> Enter addresses you own separated by commas: Bob
 mesclador> Your designated Mesclador drop box is [a1baa69c-d634-4d16-be7a-6bbd56270777]
 mesclador> Enter addresses you own separated by commas: exit
 mesclador> Saved registry state to: <>
 ```
 
  ## CLI Params
  - `registry_json_file`: path of *json* file containing the state of their user-owned addresses and the associated drop box
  - `kafka_config_file`: kafka configuration file containing *consumer* and *producer* configuration properties
  
  ### Provided registry and config files
  - [kafka_config.conf](https://jobcoin.gemini.com/polyester);
