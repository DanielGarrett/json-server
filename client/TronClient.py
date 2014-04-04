import json
import urllib2
import time
import random
class direction:
    up = 0
    down = 1
    left = 2
    right = 3
    dead = 4
    freshly_dead = 5

class field_type:
    _open = 0
    p0car1 = 1
    p0car2 = 2
    p0car3 = 3
    p1car1 = 4
    p1car2 = 5
    p1car3 = 6
    wall = 7

class tron_car:
    def __init__(self, field, x, y, direct, player):
        self.car_type = field
        self.x = x
        self.y = y
        self.dir = direct
        self.player = player

    def move(self):
        if self.dir == direction.up:
            self.y -= 1
        elif self.dir == direction.down:
            self.y += 1
        elif self.dir == direction.left:
            self.x -= 1
        elif self.dir == direction.right:
            self.x += 1

    def to_json(self):
        obj = {}
        obj['CARTYPE'] = self.car_type
        obj['DIRECTION'] = self.dir
        obj['X'] = self.x
        obj['Y'] = self.y
        obj['PLAYERID'] = self.player
        return json.dumps(obj)



    @staticmethod
    def from_json(string):
        obj = json.loads(string)
        return tron_car(obj['CARTYPE'], obj['X'], obj['Y'],
                        obj['DIRECTION'], obj['PLAYERID'])

class tron_game_state:
    def __init__(self, string, player_num):
        self.player_num = player_num
        obj = json.loads(string)
        self.running = obj['RUNNING']
        cars = [tron_car.from_json(json.dumps(car)) for car in obj['CARS']]
        self.my_cars = [car for car in cars if str(car.player) == str(player_num)]
        self.their_cars = [car for car in cars if car.player != player_num]
        self.winner = obj['WHOWON']
        self.tick = obj['TICK']
        self.board = obj['BOARD']

    def can_move_up(self, car):
        return self.board[car.x][car.y-1] == 0 and car.dir != direction.dead

    def can_move_down(self, car):
        return self.board[car.x][car.y+1] == 0 and car.dir != direction.dead

    def can_move_left(self, car):
        return self.board[car.x-1][car.y] == 0 and car.dir != direction.dead

    def can_move_right(self, car):
        return self.board[car.x+1][car.y] == 0 and car.dir != direction.dead

    def can_move_forward(self, car):
        if car.dir == direction.up:
            return self.can_move_up(car)
        elif car.dir == direction.down:
            return self.can_move_down(car)
        elif car.dir == direction.left:
            return self.can_move_left(car)
        elif car.dir == direction.right:
            return self.can_move_right(car)

class tron_client:
    connect_str = '/connect'
    move_str = '/game/move'
    status_str = '/game/status'
    def send_request(self, address, request):
        head = {'Content-type':'application/json'}
        req = urllib2.Request(address, request, head)
        f = urllib2.urlopen(req)
        response = f.read()
        f.close()
        return response
        
    def __init__(self, host, port):
        self.url = 'http://'+host+':'+str(port)
        initial = json.loads(self.send_request(self.url+tron_client.connect_str, '{}'))
        self.player = initial['ID']
        self.auth = initial['AUTH']
        self.game = initial['GAMEID']

    def move(self, cars):
        move_list = [{"CAR":car.car_type,"DIR":car.dir} for car in cars]
        req = json.dumps({"MOVES":move_list, 'GAMEID':self.game,
                    'ID':self.player, 'AUTH':self.auth})
        res = json.loads(self.send_request(self.url+tron_client.move_str, req))
        return tron_game_state(json.dumps(res['STATUS']),self.player)

    def get_status(self):
        req = json.dumps({'GAMEID':self.game, 'ID':self.player, 'AUTH':self.auth})
        res = self.send_request(self.url+tron_client.status_str, req)
        return tron_game_state(res,self.player)
        
client = tron_client('localhost', 8000)
status = client.get_status()
while status.running:
    time.sleep(.05)
    for car in status.my_cars:
        if status.can_move_forward(car):
            if status.can_move_down(car) and random.randrange(50) == 0:
                car.dir = direction.down
            elif status.can_move_left(car) and random.randrange(50) == 0:
                car.dir = direction.left
            elif status.can_move_right(car) and random.randrange(50) == 0:
                car.dir = direction.right
            elif status.can_move_up(car) and random.randrange(50) == 0:
                car.dir = direction.up
        else:
            if status.can_move_down(car):
                car.dir = direction.down
            if status.can_move_left(car):
                car.dir = direction.left
            if status.can_move_right(car):
                car.dir = direction.right
            if status.can_move_up(car):
                car.dir = direction.up
    status = client.move(status.my_cars)
    print status.my_cars
                
        
