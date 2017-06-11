# Week 5 - Hadoop
* WordCount
* GooglePlay Crawler in Nutch

## WordCount 
Requirements:

* 1. Stores into sequence file
* 2. Compress output file
* 3. custom partitioner
* 4. dict text for Chinese translation


### 1. Stores into sequence file

Flags in WordCount.java:

```
  public static final boolean PARTITION = false;
  public static final boolean COMPRESS = false;
  public static final boolean SEQUENCE = true;
  public static final boolean DICTIONARY = false;
```

Input:

```
Apple Cat Banana Bag Dog Apple Cat Elizabeth Jiujitsu Chicago Aeon Acne
我 爱
猫 狗
```

Output:

```
SEQorg.apache.hadoop.io.Text org.apache.hadoop.io.IntWritableU����'~�7���	Acne	Aeon
ApplBag
       BananCat
              ChicagDog
       Jiujits   猫hadoop
```

### 2. Compress output file

Flags in WordCount.java:

```
  public static final boolean PARTITION = false;
  public static final boolean COMPRESS = true;
  public static final boolean SEQUENCE = false;
  public static final boolean DICTIONARY = false;
```

Input:

```
Apple Cat Banana Bag Dog Apple Cat Elizabeth Jiujitsu Chicago Aeon Acne
我 爱
猫 狗
```	

Output:

```
sL�K�4�rL��Q9��F\N��@�SbΉ%@A�����| �%$횓Y���Z�d{e�fe����:&��d�tٳH�&�sjhadoop
```

### 3. Custom partitioner

* 2 reducers
* Word starts with A and B always go to same reduce

Flags in WordCount.java:

```
  public static final boolean PARTITION = true;
  public static final boolean COMPRESS = false;
  public static final boolean SEQUENCE = false;
  public static final boolean DICTIONARY = false;
```

Input:

```
Apple Cat Banana Bag Dog Apple Cat Elizabeth Jiujitsu Chicago Aeon Acne
我 爱
猫 狗
```

Output:

> cat part-r-00000

```
Acne	1
Aeon	1
Apple	2
Bag	1
Banana	1
```

> cat part-r-00001

```
Cat	2
Chicago	1
Dog	1
Elizabeth	1
Jiujitsu	1
我	1
爱	1
狗	1
猫	1
```

### 4. Dict text for Chinese translation

* Send a dict txt file to distributed cache, so for Chinese word, it will use dict file to translate first, then do the count.

Flags in WordCount.java:

```
  public static final boolean PARTITION = false;
  public static final boolean COMPRESS = false;
  public static final boolean SEQUENCE = false;
  public static final boolean DICTIONARY = true;
```

Dictionary file;

```
我:I
爱:LOVE
猫:Cat
狗:Dog
```

Input:

```
Apple Cat Banana Bag Dog Apple Cat Elizabeth Jiujitsu Chicago Aeon Acne
我 爱
猫 狗
```

Output:

```
Acne	1
Aeon	1
Apple	2
Bag	1
Banana	1
Cat	3
Chicago	1
Dog	2
Elizabeth	1
I	1
Jiujitsu	1
LOVE	1
```

## Nutch
Please check the attached screenshots.