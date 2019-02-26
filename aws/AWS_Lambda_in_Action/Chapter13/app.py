from chalice import Chalice

app = Chalice(app_name='greetingsOnDemand')

@app.route('/')
def index():
    return {'hello': 'world'}

@app.route('/greet/{name}')
def hello_name(name):
    return {'hello': name}
