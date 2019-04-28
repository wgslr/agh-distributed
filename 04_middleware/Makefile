slice: api/*.ice
	mkdir -p {bank,currencytracker}/generated
	rm -rf {bank,currencytracker}/generated/*
	slice2java --output-dir bank/generated $^
	slice2java --output-dir currencytracker/generated $^

