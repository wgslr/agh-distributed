cat 2019-02-06_goodreads_library_export.csv | grep -E '^[0-9]+,"' | cut -d'"' -f 2 > /home/wojciech/uczelnia/rozproszone/agh-distributed/05_akka/assets/titles.txt

python3 gen.py < titles.txt > prices.txt

sed -i 's/(.*)//g' assets/**

tac db2 | sponge db2
