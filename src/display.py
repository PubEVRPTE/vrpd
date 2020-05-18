import pygame
import os
import sys
from math import *

path = os.getcwd()
path1 = path + "/instances/Instances/150.30.1.txt"
path2 = path + "/vehicleRoute.txt"
path3 = path + "/dronesRoute.txt"
path4 = path + "/instances/RouteData.txt"

# load all points
pos = [[400, 400]]
with open(path1, "r") as f:
    for line in f.readlines()[2:]:
        lineList = line.split("  ")
        tempPos = [int(20*float(lineList[0])) + 400, int(20*float(lineList[1])) + 400]
        pos.append(tempPos)

# load vehicleRoute
vehicleRoute = []
with open(path2, "r") as f:
    for line in f.readlines():
        line.strip()
        route = []
        lineList = line.split(" ")
        temp = [int(x) for x in lineList[:-1]]
        vehicleRoute.append(temp)

# load droneRoute
droneRoute = []
with open(path3, "r") as f:
    for line in f.readlines():
        line.strip()
        lineList = line.split(" ")
        temp = [int(x) for x in lineList]
        droneRoute.append(temp)
for route in vehicleRoute:
    for sortie in droneRoute:
        if sortie[1] in route:
            droneRoute.remove(sortie)

# load velocity
velocity = []
with open(path4, "r") as f:
    for line in f.readlines()[:2]:
        line = line.split()
        nums = line[1].split('/')
        num = float(nums[0]) / float(nums[1])
        velocity.append(num)
vv = velocity[0]*10
vd = velocity[1]*10

# init screen
pygame.init()
screencaption = pygame.display.set_caption('Solution Display')
screen = pygame.display.set_mode([1000, 800])
screen.fill([255, 255, 255])

# draw vehicleRoute
r = 0
g = 0
b = 0
for route in vehicleRoute:
    Route = []
    for poi in route:
        if poi == 0:
            pygame.draw.circle(screen, [255, 0, 0], pos[poi], 10, 5)
        else:
            pygame.draw.circle(screen, [0, 0, 0], pos[poi], 4, 1)
        Route.append(pos[poi])
    pygame.draw.lines(screen, [r, g, b], True, Route, 1)

# draw droneRoute
for poi in droneRoute:
    Route = []
    pygame.draw.rect(screen, [0, 0, 255], pos[poi[1]] + [6, 6], 3)
    for i in poi:
        Route.append([pos[i][0] + 3, pos[i][1] + 3])
    pygame.draw.lines(screen, [0, 255, 0], False, Route, 1)
pygame.display.flip()
pygame.image.save(screen, "background.png")

# load img
background = pygame.image.load('background.png')
vehicle = pygame.image.load('vehicle.png')
drone = pygame.image.load('drone.png')
screen.blit(background, (0, 0))
pygame.display.update()

# init para
a = 0  # route flag
b = 1  # vpoi flag
c = 0  # move flag
pre = 0
clock = pygame.time.Clock()
vpoi = pos[vehicleRoute[a][b]]
start = pos[0]
vstart = pos[0]
dstart = pos[0]
cf1 = 0
cf2 = 0

# 计算角度
def tri(p1, p2):
    h1 = p2[0] - p1[0]
    h2 = p2[1] - p1[1]
    l = sqrt(h1**2 + h2**2)
    cosp = h1 / l
    sinp = h2 / l
    return cosp, sinp, l


# run
while True:
    screen.blit(background, (0, 0))
    cosx, sinx, dis = tri(pos[pre], vpoi)
    cf = int(dis / vv)
    # 超过cflag帧则换目标点
    if c == max(cf, cf2):
        pre = vehicleRoute[a][b]
        c = 0
        b += 1
        if b == len(vehicleRoute[a]):
            a += 1
            b = 1
            pre = vehicleRoute[a][0]
        vpoi = pos[vehicleRoute[a][b]]
        start = pos[pre]
        vstart = pos[pre]
        dstart = pos[pre]
        cf1 = 0
        cf2 = 0
    # 跑完所有路径换路
    if a == len(vehicleRoute):
        sys.exit()
    # 检验是否释放无人机
    for sortie in droneRoute:
        if vehicleRoute[a][b] in sortie and pre in sortie:
            dx0 = cosx * vv
            dy0 = sinx * vv
            cosx1, sinx1, dis1 = tri(pos[pre], pos[sortie[1]])
            cosx2, sinx2, dis2 = tri(pos[sortie[1]], vpoi)
            dx1 = cosx1 * vd
            dy1 = sinx1 * vd
            dx2 = cosx2 * vd
            dy2 = sinx2 * vd
            cf1 = int(dis1 / vd)
            cf2 = cf1 + int(dis2 / vd)
            screen.blit(background, (0, 0))
            if c < cf:
                vstart = [vstart[0] + dx0, vstart[1] + dy0]
            else:
                vstart = vpoi
            if c < cf1:
                dstart = [dstart[0] + dx1, dstart[1] + dy1]
            elif c == cf1:
                dstart = pos[sortie[1]]
            elif cf1 < c < cf2:
                dstart = [dstart[0] + dx2, dstart[1] + dy2]
            else:
                dstart = vpoi
            screen.blit(vehicle, [vstart[0]-8, vstart[1]-8])
            screen.blit(drone, [dstart[0]-10, dstart[1]-10])
            break
    else:
            dx = cosx * vv
            dy = sinx * vv
            start = [start[0] + dx, start[1] + dy]
            screen.blit(vehicle, [start[0]-8, start[1]-8])
    c += 1
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            sys.exit()

    pygame.display.update()
    clock.tick(300)
