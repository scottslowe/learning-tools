import json

print('Loading function')

def lambda_handler(event, context):
    print("Received event: " +
        json.dumps(event, indent=2))
    if 'greet' in event:
        greet = event['greet']
    else:
        greet = 'Hello'
    if 'name' in event:
        name = event['name']
    else:
        name = 'World'
    greetings = greet + ' ' + name + '!'
    print(greetings)
    return greetings
