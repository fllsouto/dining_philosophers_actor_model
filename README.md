# Dining Philosophers Actor Model

## About

This software was developed as a final project for Computer Science  course at IME-USP. They consist in a study about concurrency, focused on Actor Model and It's implementation using Akka Framework, applied to Dinner Philosophers problem and its resource sharing competition.

My personal page with my monography (in Brazilian Portuguese) can be [found here](https://www.linux.ime.usp.br/~fsouto/mac0499/). My personal contact is fllsouto@gmail.com or @soutofx on Twitter.

## Setup Environment (Ubuntu 16.04 LTS)

### Java, Scala and Akka

```bash
# In project root folder

# Installing Java 8

sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
java -version

# Installing Sbt (Scala build tool)

echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt

# Will install Scala, Akka and another dependencies
sbt

```

### Ruby and Gems

```bash
# In project root folder

# Install Ruby on Ubuntu 16.04
# Based on https://gorails.com/setup/ubuntu/16.04

sudo apt-get install -y git-core curl zlib1g-dev build-essential libssl-dev libreadline-dev libyaml-dev libsqlite3-dev sqlite3 libxml2-dev libxslt1-dev libcurl4-openssl-dev python-software-properties libffi-dev

sudo apt-get install -y libgdbm-dev libncurses5-dev automake libtool bison libffi-dev
gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3
curl -sSL https://get.rvm.io | bash -s stable
source ~/.rvm/scripts/rvm
rvm install 2.3.1
rvm use 2.3.1 --default
ruby -v

# Install Bundler
gem install bundler


# Install Gems dependencies
cd ruby_scripts/
bundle install
```

## How to run simulation

### By commmand line, only one simulation

```bash
# In project root folder

sbt "run-main br.usp.ime.fllsouto.diningPhilosophers.DinningPhilosophers <T> <A> <O>"

# Where:
# T: Simulation time, in seconds
# A: Algorithm chosen ([R]esource Hierarchy, [W]aiter, [C]handyMisra)
# O: Fake random thinking and eating time. [Y] to use 5 seconds for both options, and [N] to generate a random intereger between 1~10 to each option

# Example

sbt "run-main br.usp.ime.fllsouto.diningPhilosophers.DinningPhilosophers 3600 C Y"
```

### By simulation menu, only one simulation

```bash
sbt "run-main br.usp.ime.fllsouto.diningPhilosophers.DinningPhilosophers"

# A prompt menu will ask for the input information to run the simulation
```

### Automatized, using ruby script

It's possible to define how much time the simulation will execute, which algorithms will be used and the available options. All this configuration are setted by `DinnerAutomaticExecuter` class on `src/automation/dinner_automatic_executer.rb`.

```bash
# In project root folder

ruby ruby_scripts/src/automation/dinner_automatic_executer.rb <debug>

# Where:
# --debug will print all the simulation log
```


## How to generate graphics
```


By Fellipe S Sampaio
