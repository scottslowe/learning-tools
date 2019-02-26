# Node TODO for AWS

![Node TODO for AWS](./nodetodo.png?raw=true "Node TODO for AWS")

Install the dependencies ...

	npm install

... and run nodetodo

	node index.js --help

## usage

### user

#### add

	node index.js user-add <uid> <email> <phone>

	node index.js user-add michael michael@widdix.de 0123456789

#### remove

	node index.js user-rm <uid>

	node index.js user-rm michael

#### list

	node index.js user-ls

#### show

	node index.js user <uid>

	node index.js user michael

### task

#### add

	node index.js task-add <uid> <description> [<category>] [--dueat=<yyyymmdd>] 

	node index.js task-add michael "plan lunch" --dueat=20150522

####  remove

	node index.js task-rm <uid> <tid>

	node index.js task-rm michael 1432187491647

#### list

	node index.js task-ls <uid> [<category>] [--overdue|--due|--withoutdue|--futuredue|--dueafter=<yyyymmdd>|--duebefore=<yyyymmdd>] [--limit=<limit>] [--next=<id>]

	node index.js task-ls michael

#### mark as done

	node index.js task-done <uid> <tid>

	node index.js task-done michael 1432187491647

## schema

### user

	aws dynamodb create-table --table-name todo-user --attribute-definitions AttributeName=uid,AttributeType=S --key-schema AttributeName=uid,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

#### key

* HASH: uid

#### values

* uid: string
* email: string
* phone: string

### task

	aws dynamodb create-table --table-name todo-task --attribute-definitions AttributeName=uid,AttributeType=S AttributeName=tid,AttributeType=N --key-schema AttributeName=uid,KeyType=HASH AttributeName=tid,KeyType=RANGE --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

	aws dynamodb update-table --table-name todo-task --attribute-definitions AttributeName=uid,AttributeType=S AttributeName=tid,AttributeType=N AttributeName=category,AttributeType=S --global-secondary-index-updates '[{"Create": {"IndexName": "category-index", "KeySchema": [{"AttributeName": "category", "KeyType": "HASH"}, {"AttributeName": "tid", "KeyType": "RANGE"}], "Projection": {"ProjectionType": "ALL"}, "ProvisionedThroughput": {"ReadCapacityUnits": 5, "WriteCapacityUnits": 5}}}]'

#### key

* HASH: uid
* RANGE: tid

#### values

* uid: string
* tid: number (time stamp)
* category: string (optional)
* description: string
* due: number (yyyymmdd)
* created: number (yyyymmdd)
* completed: number (yyyymmdd)
