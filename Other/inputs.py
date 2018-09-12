#!/usr/bin/python3
import argparse
import sys, os, re, time, urllib3, json, io
from bs4 import BeautifulSoup


input_stream = io.TextIOWrapper(sys.stdin.buffer, encoding='UTF-8')

OUTPUT_FILE = 'output.json' 
SLEEP_SECONDS  = 1.4 # between page downloads
FIT_URL = "http://www.fit.vutbr.cz"
BIT_URL  = FIT_URL + "/study/bc/stplan-l.php.cs?id=194"

def get_webpage(url):
    http = urllib3.PoolManager()
    response = http.request('GET', url)
    html_data = response.data.decode('ISO-8859-2')
    return BeautifulSoup(html_data, "html.parser")

def parse_programme(url):
    soup = get_webpage(url)
    res = soup.find_all("table")
    res.pop(0)      # remove 'content-table' - wrapper around page content
    table = res[0]  # first table contains our data

    course_list = []
    current_year = '1'
    current_term = 'Z' # zimni

    for row in table.find_all('tr'):
        hrefs = row.find_all('a')
        if (len(hrefs) == 2):   # line contains course info
            course_info = row.find_all(['td','th'])
            course_url = FIT_URL+hrefs[0].get('href')
            if course_url[-5] == "=":
                course_id = course_url[-4:]
            else:
                course_id = course_url[-5:]
            course = {
                'zkratka': course_info[0].text,
                'typ':     course_info[1].text,
                'zak':     course_info[4].text,
                'url':     course_url,
                'id':      course_id, # last 5 characters of url = id,
                'rocnik':  current_year,
                'semestr': current_term
            }
            course_list.append(course)
        else:
            try:
                ths = row.find_all('th')
                year_term = re.match('.*[Rr]očník\s*(\d)?,\s*semestr\s*(\S+)', ths[0].find_all('h4')[0].text)
                current_year = year_term.group(1) if year_term.group(1) else 'X'
                current_term = 'Z' if year_term.group(2) == 'zimní' else 'L'
            except (AttributeError, IndexError):
                pass

    return course_list
#
#

def parse_fitpages():
    TABLE_INDEX = 0 # 0 = aktualny rok, 1 = minuly rok

    soup = get_webpage(FIT_URL + '/study/msc/.cs')
    res = soup.find_all("table")
    heading = soup.find_all("h2")[TABLE_INDEX]
    year = re.match('.*([\d]{4})/([\d]{4})', heading.text).group(1)

    res.pop(0)      # remove 'content-table' - wrapper around page content
    table = res[TABLE_INDEX]  # there are two tables - first one contains data for current year

    study_programs = {} # url to study plan for each study program
    study_programs_name = {} #name of program
    courses = []        # list of courses for each study program

    # parse table with study programms
    for row in table.find_all('tr'):
        prog = row.find_all('th')[0].text
        for td in row.find_all('td'):
            study_programs_name[prog] = td.text
            for a in td.find_all('a'):
                study_programs[prog] = (FIT_URL + '/study/msc/' + a.get('href'))

    #print([prog for prog in study_programs])
    for prog in study_programs:
        url = study_programs[prog]
        courses.append({ 'name' : prog, 'full_name' : study_programs_name[prog], 'courses' : parse_programme(url) })
        print('Study program parsed: ' + prog)
        time.sleep(SLEEP_SECONDS)

    courses.append({ 'name' : 'BIT', 'full_name' : 'Informační technologie', 'courses' : parse_programme(BIT_URL) })

    courses_list = []

    for prog in courses:
        print(prog['name'] + ' => ' )
        for course in prog['courses']:
            
            parsed = set().union((d.get('zkratka') for d in courses_list))   
            if course['zkratka'] not in parsed: # download webpage of each course only once
                soup = get_webpage(course['url'])
                # soup = get_webpage("www.fit.vutbr.cz/study/course-l.php?id=12295")
                course_name = soup.find_all("h1")[1].text
                time.sleep(SLEEP_SECONDS)
                print('Parsing: ' + course['zkratka'])

                indices = [-1,-2,-3, -4, -5, -6]
                while indices:
                    idx = indices[0]
                    try:
                        inner_table = [row for row in soup.find_all('table')[idx]]
                        values = [int(td.text) for td in inner_table[1].find_all('td') ] # first row contains the numbers
                        garant = ""
                        prednas = []
                        cvicici = []

                        for table in soup.find_all('table')[0]:
                            trs = table.find_all("tr")
                            
                            for tr in trs:
                                ths = tr.find_all("th")
                                
                                for th in ths:
                                    thstr = th.string
                                    if thstr == "Garant:":
                                        garant = tr.find_all("td")[0].find_all("a")[0].text
                                    elif thstr == "Přednášející:":
                                        td = tr.find_all("td")[0]
                                        prednas = reg(td)
                                            # search for <br/> 
                                            # else:
                                                # print(line)
                                    elif thstr == "Cvičící:":
                                        td = tr.find_all("td")[0]
                                        cvicici = reg(td)
                                
                        for cvic in cvicici:
                            if cvic not in prednas:
                                prednas.append(cvic)

                        c = {
                            'zkratka': course['zkratka'],
                            'nazev'  : course_name,
                            'predn'  : values[0],
                            'cv'     : values[1],
                            'lab_cv' : values[2],
                            'poc_cv' : values[3],
                            'jina'   : values[4],
                            'id'     : course['id'],
                            'garant' : garant,
                            'prednas': prednas
                            # 'cvicici': cvicici
                        }
                        # print(c)
                        courses_list.append(c)

                    except (AttributeError, ValueError):
                        indices.pop(0)
                        if indices:
                            continue
                    break

    for c in courses:   
        for cc in c['courses']:
            del cc['url'] # urls are not needed any more
    result = {
        'year' : year,
        'study_programs' : courses,
        'courses' : courses_list
    }

    with open(OUTPUT_FILE, 'w') as outfile:
        json.dump(json.loads(json.dumps(result)), outfile, skipkeys=False, ensure_ascii=False, check_circular=True, allow_nan=True, cls=None, indent=2)

# 
#

# parse all teachers
def reg(td):
    string = []

    # find links with teachers
    tdval = td.find_all("a")
    #else there is teacher without link, so remove teacher' place
    if len(tdval) == 0 or not str(td)[4] == '<':
        strtd = str(td)
        f = strtd.find("<br>")
        #if there is br, so there is more values
        if f > 0:
            strtd = strtd[4:f]
            string.append(strtd[:strtd.rfind(',')])
        #just one value
        else:
            string.append(td.text[:td.text.rfind(',')])
    # if there is at least one link
    else:       
        string.append(tdval[0].text)

    # for all new line 
    for br in td.find_all("br"):
        
        rec = reg(br)
        for r in rec:
            string.append(r)
        
        #find only first br
        break

    return string



def parse_terms():
    res = {
        'year' : 0,
        'term' : '',
        'data' : []
    }

    soup = BeautifulSoup(input_stream, "html.parser")
    term = soup.find('select', {"name":"sem"}).find_all('option', attrs={"selected":True})[0].text
    yr = soup.find("input", {"id": "id_rok"})['value']
    #print("Rok: " + yr + " semestr: " + term)

    res['year'] = int(yr)
    res['term'] = term

    rows=soup.find_all('table')[2].find_all('tr')
    rows.pop(0) # first row doesn't contain any data
    rows.pop(0) # second row contains only headers
    for row in rows:
        course = row.find_all('th')[0].find_all('a')[0].text
        data = row.find_all('td')

        item = {
            'stud'  : data[ 3].text,
            #'term' : data[ 4].text,
            #'zap'  : data[ 5].text.replace(u'\xa0', u' '),
            'exams' : data[ 6].text.replace(u'\xa0', u' '),
            'first' : data[ 7].text.replace(u'\xa0', u' '),
            'second': data[ 8].text.replace(u'\xa0', u' '),
            'third' : data[ 9].text.replace(u'\xa0', u' '),
            'fourth': data[10].text.replace(u'\xa0', u' '),
            'fifth' : data[11].text.replace(u'\xa0', u' '),
            'sixth' : data[12].text.replace(u'\xa0', u' ')
        }

        obj = {
            'course' : course,
            'values' : item
        }
        res['data'].append(obj)

    #with open(OUTPUT_FILE, 'w') as outfile:
    jsonstr = json.dumps(res, skipkeys=False, ensure_ascii=False, check_circular=True, allow_nan=True, cls=None, indent=None, separators=(',', ':'))
    print(jsonstr)
    return
#
#

def parse_collisions():

    # with open(filename, 'r', encoding='ISO-8859-2') as file:
    soup = BeautifulSoup(input_stream, "html.parser")
    term = soup.find('select', {"name":"sem"}).find_all('option', attrs={"selected":True})[0].text
    yr = soup.find("input", {"id": "id_year"})['value']
    #print("Rok: " + yr + " semestr: " + term)
    rows = soup.find_all('table')[3].find_all('tr')

    course_list = []
    for th in rows[0].find_all('th'):
        course_list.append(th.text)

    course_list.pop(0) # remove &nbsp
    course_list.pop(0) # remove 'Stud'
    #print(course_list)

    rows.pop(0) # first line contains course list
    rows.pop(0) # second line contains course ids
    res=[]

    for row_idx,row in enumerate(rows):
        # print(row.find_all('td'))
        # print('\n')
        collisons = []
        cells = [x for x in row.find_all('td')]
        course_size = int(cells[0].text) 
        cells = cells[1:] # remove first column containing number of students in this course

        for i,td in enumerate(cells):
            try:
                collisons.append({ 'course': course_list[i], 'value': int(td.text) })
            except ValueError:
                collisons.append({ 'course': course_list[i], 'value': 0 })

        res.append({ 'course': course_list[row_idx], 'stud': course_size, 'collisions': collisons })
    
    out={}
    out['data']=res
    out['year']=yr
    out['term']=term
    json.dump(json.loads(json.dumps(out)), sys.stdout, skipkeys=False, ensure_ascii=False, check_circular=True, allow_nan=True, cls=None, indent=None, separators=(',', ':'))
    return
#
#

def parse_enrollments(dirname):
    out = []
    for filename in os.listdir(dirname):
        with open(dirname+'/'+filename, 'r', encoding='ISO-8859-2') as file:
           soup = BeautifulSoup(file.read(), "html.parser")
           course = soup.find("title").text.split("/")[0]
           #print(course)
           regStuds = soup.find_all('table')[8].find_all('tr')

           allRegStudents = [x for x in regStuds]
           allRegStudents = allRegStudents[1: len(allRegStudents)-1]
           students = []
           for regStudent in allRegStudents:
               cells = [x for x in regStudent.find_all('td')]
               studId = cells[0].text    # typeof(ID) == string
               studName = cells[1].text
               studLogin = cells[2].text 
               studType = cells[4].text 
               students.append({'login' : studLogin, 'id': studId, 'name' : studName, 'type': studType})
           
           course = {'course' : course, 'students' : students}
           out.append(course)

    with open(OUTPUT_FILE, 'w') as outfile:
        json.dump(json.loads(json.dumps(out)), outfile, skipkeys=False, ensure_ascii=False, check_circular=True, allow_nan=True, cls=None, indent=2)

    return
#
#

def parse_obory(filename):


    soup = BeautifulSoup(input_stream.read(), "html.parser")
    rows = soup.find_all('table')[3].find_all('tr')

    item_list = [th.text for th in rows[0].find_all('th')]
    item_list.pop(0) # first cell is empty, remove it
    rows.pop(0) # first line item_list course list

    data = []
    for r in rows:
        tds = r.find_all(['td','th'])
        course = tds.pop(0).text
        for i, val in enumerate(tds):
            data.append({
                'course': course, 
                'obor' : item_list[i],
                'value': tds[i].text.replace(u'\xa0', u' ')
            })
    out = {
        'data': data,
        'obory': item_list
    }

    json.dump(json.loads(json.dumps(out)), sys.stdout, skipkeys=False, ensure_ascii=False, check_circular=True, allow_nan=True, cls=None, indent=None, separators=(',', ':'))
    return
#
#

parser = argparse.ArgumentParser()
parser.add_argument('-t', action='store_true', help='Terminy')
parser.add_argument('-k', action='store_true', help='Kolize')
parser.add_argument('-w', '--web', action='store_true', help='Stahnout a naparsovat studijni plany z webstranek FIT')
parser.add_argument('-o', metavar='output.json',  help='Vystupni soubor')
parser.add_argument('-d', metavar='directory', help='Adresar se seznamy zapsanych studentu')
parser.add_argument('-b', action='store_true', help='Obory')
args = parser.parse_args()

if args.o:
    OUTPUT_FILE = args.o

if args.k:
    # if not os.path.exists(args.k):
    #     print("Cannot open file: " + args.k, file=sys.stderr)
    #     exit(-1)
    parse_collisions()

if args.t:
    # if not os.path.exists(args.t):
    #     print("Cannot open file: " + args.t, file=sys.stderr)
    #     exit(-1)
    parse_terms()

if args.web:
    parse_fitpages()

if args.d:
    if not os.path.exists(args.d):
        print("Cannot open file: " + args.d, file=sys.stderr)
        exit(-1)
    parse_enrollments(args.d)

if args.b:
    # if not os.path.exists(args.b):
    #     print("Cannot open file: " + args.b, file=sys.stderr)
    #     exit(-1)
    parse_obory(args.b)
