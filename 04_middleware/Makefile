slice: api/*.ice
	mkdir -p {bank,bankclient}/generated
	rm -rf {bank,bankclient}/generated/*
	slice2java --output-dir bank/generated $^
	#slice2py --output-dir bankclient/generated $^
	slice2py --output-dir bankclient $^

