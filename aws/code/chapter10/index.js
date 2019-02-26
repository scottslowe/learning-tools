var fs = require('fs');
var docopt = require('docopt');
var moment = require("moment");
var AWS = require('aws-sdk');
var db = new AWS.DynamoDB({
	"region": "us-east-1"
});

var cli = fs.readFileSync('./cli.txt', {"encoding": "utf8"});
var input = docopt.docopt(cli, {"version": "1.0", "argv": process.argv.splice(2)});

function getValue(attribute, type) {
	if (attribute === undefined) {
		return null;
	}
	return attribute[type];
}

function mapTaskItem(item) {
	return {
		"tid": item.tid.N,
		"description": item.description.S,
		"created": item.created.N,
		"due": getValue(item.due, 'N'),
		"category": getValue(item.category, 'S'),
		"completed": getValue(item.completed, 'N')
	};
}

function mapUserItem(item) {
	return {
		"uid": item.uid.S,
		"email": item.email.S,
		"phone": item.phone.S
	};
}

if (input['user-add'] === true) {
	var params = {
		"Item": {
			"uid": {
				"S": input['<uid>']
			},
			"email": {
				"S": input['<email>']
			},
			"phone": {
				"S": input['<phone>']
			}
		},
		"TableName": "todo-user",
		"ConditionExpression": "attribute_not_exists(uid)"
	};
	db.putItem(params, function(err) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('user added with uid ' + input['<uid>']);
		}
	});
} else if (input['user-rm'] === true) {
	var params = {
		"Key": {
			"uid": {
				"S": input['<uid>']
			}
		},
		"TableName": "todo-user"
	};
	db.deleteItem(params, function(err) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('user removed with uid ' + input['<uid>']);
		}
	});
} else if (input['user-ls'] === true) {
	var params = {
		"TableName": "todo-user",
		"Limit": input['--limit']
	};
	if (input['--next'] !== null) {
		params.ExclusiveStartKey = {
			"uid": {
				"S": input['--next']
			}
		};
	}
	db.scan(params, function(err, data) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('users', data.Items.map(mapUserItem));
			if (data.LastEvaluatedKey !== undefined) {
				console.log('more users available with --next=' + data.LastEvaluatedKey.uid.S);
			}
		}
	});
} else if (input['user'] === true) {
	var params = {
		"Key": {
			"uid": {
				"S": input['<uid>']
			}
		},
		"TableName": "todo-user"
	};
	db.getItem(params, function(err, data) {
		if (err) {
			console.error('error', err);
		} else {
			if (data.Item) {
				console.log('user with uid ' + input['<uid>'], mapUserItem(data.Item));
			} else {
				console.error('user with uid ' + input['<uid>'] + ' not found');
			}
		}
	});
} else if (input['task-add'] === true) {
	var tid = Date.now();
	var params = {
		"Item": {
			"uid": {
				"S": input['<uid>']
			},
			"tid": {
				"N": tid.toString()
			},
			"description": {
				"S": input['<description>']
			},
			"created": {
				"N": moment().format("YYYYMMDD")
			}
		},
		"TableName": "todo-task",
		"ConditionExpression": "attribute_not_exists(uid) and attribute_not_exists(tid)"
	};
	if (input['--dueat'] !== null) {
		params.Item.due = {
			"N": input['--dueat']
		};
	}
	if (input['<category>'] !== null) {
		params.Item.category = {
			"S": input['<category>']
		};
	}
	db.putItem(params, function(err) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('task added with tid ' + tid);
		}
	});
} else if (input['task-rm'] === true) {
	var params = {
		"Key": {
			"uid": {
				"S": input['<uid>']
			},
			"tid": {
				"N": input['<tid>']
			}
		},
		"TableName": "todo-task"
	};
	db.deleteItem(params, function(err) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('task removed with tid ' + input['<tid>']);
		}
	});
} else if (input['task-ls'] === true) {
	var params = {
		"KeyConditionExpression": "uid = :uid",
		"ExpressionAttributeValues": {
			":uid": {
				"S": input['<uid>']
			}
		},
		"TableName": "todo-task",
		"Limit": input['--limit']
	};
	if (input['--next'] !== null) {
		params.KeyConditionExpression += ' AND tid > :next';
		params.ExpressionAttributeValues[':next'] = {
			"N": input['--next']
		};
	}
	if (input['--overdue'] === true) {
		params.FilterExpression = "due < :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": moment().format("YYYYMMDD")};
	} else if (input['--due'] === true) {
		params.FilterExpression = "due = :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": moment().format("YYYYMMDD")};
	} else if (input['--withoutdue'] === true) {
		params.FilterExpression = "attribute_not_exists(due)";
	} else if (input['--futuredue'] === true) {
		params.FilterExpression = "due > :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": moment().format("YYYYMMDD")};
	} else if (input['--dueafter'] !== null) {
		params.FilterExpression = "due > :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": input['--dueafter']};
	} else if (input['--duebefore'] !== null) {
		params.FilterExpression = "due < :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": input['--duebefore']};
	}
	if (input['<category>'] !== null) {
		if (params.FilterExpression === undefined) {
			params.FilterExpression = '';
		} else {
			params.FilterExpression += ' AND ';
		}
		params.FilterExpression += 'category = :category';
		params.ExpressionAttributeValues[':category'] = {
			"S": input['<category>']
		};
	}
	db.query(params, function(err, data) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('tasks', data.Items.map(mapTaskItem));
			if (data.LastEvaluatedKey !== undefined) {
				console.log('more tasks available with --next=' + data.LastEvaluatedKey.tid.N);
			}
		}
	});
} else if (input['task-la'] === true) {
	var params = {
		"KeyConditionExpression": "category = :category",
		"ExpressionAttributeValues": {
			":category": {
				"S": input['<category>']
			}
		},
		"TableName": "todo-task",
		"IndexName": "category-index",
		"Limit": input['--limit']
	};
	if (input['--next'] !== null) {
		params.KeyConditionExpression += ' AND tid > :next';
		params.ExpressionAttributeValues[':next'] = {
			"N": input['--next']
		};
	}
	if (input['--overdue'] === true) {
		params.FilterExpression = "due < :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": moment().format("YYYYMMDD")};
	} else if (input['--due'] === true) {
		params.FilterExpression = "due = :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": moment().format("YYYYMMDD")};
	} else if (input['--withoutdue'] === true) {
		params.FilterExpression = "attribute_not_exists(due)";
	} else if (input['--futuredue'] === true) {
		params.FilterExpression = "due > :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": moment().format("YYYYMMDD")};
	} else if (input['--dueafter'] !== null) {
		params.FilterExpression = "due > :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": input['--dueafter']};
	} else if (input['--duebefore'] !== null) {
		params.FilterExpression = "due < :yyyymmdd";
		params.ExpressionAttributeValues[':yyyymmdd'] = {"N": input['--duebefore']};
	}
	db.query(params, function(err, data) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('tasks', data.Items.map(mapTaskItem));
			if (data.LastEvaluatedKey !== undefined) {
				console.log('more tasks available with --next=' + data.LastEvaluatedKey.tid.N);
			}
		}
	});
} else if (input['task-done'] === true) {
	var params = {
		"Key": {
			"uid": {
				"S": input['<uid>']
			},
			"tid": {
				"N": input['<tid>']
			}
		},
		"UpdateExpression": "SET completed = :yyyymmdd",
		"ExpressionAttributeValues": {
			":yyyymmdd": {
				"N": moment().format("YYYYMMDD")
			}
		},
		"TableName": "todo-task"
	};
	db.updateItem(params, function(err) {
		if (err) {
			console.error('error', err);
		} else {
			console.log('task completed with tid ' + input['<tid>']);
		}
	});
}
