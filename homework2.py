"""
Structure:
"string" - Parse the string once
 ("string", "n") - Parse the string n times
 ("string1", ("string2", "n-i"), "n") -
     Parse string 1, then string2 n-i times, repeat n times
 Constants:
     n = n
     c = 1
"""
reference_string = (
    "0203",
    (
        "n",
        "000300606008030040200"
    ),
    "000203",
    (
        "n",
        "00003",
        (
            "n",
             "00030600800804060181181211"
         ),
        "001218161161211"
    ),
    "00"
)

reference_string2 = (
    "1",
    (
        "n",
        "2"
    ),
    (
        "n",
        "32",
    ),
    (
        "n",
        "432"
    ),
    "1"
)

reference_string3 = (
    "1222",
    (
        "n-3",
        "2"
    ),
    "3232",
    (
        "n-2",
        "32"
    ),
    "432",
    (
        "n",
        "432"
    ),
    "1"
    
)

expanded_string = "0203000300606008030040200000300606008030040200000300606008030040200000300606008030040200000300606008030040200000300606008030040200000300606008030040200000300606008030040200000300606008030040200000300606008030040200000203000030003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100121816116121100003000306008008040601811812110003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211001218161161211000030003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211001218161161211000030003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100121816116121100003000306008008040601811812110003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211001218161161211000030003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100030600800804060181181211001218161161211000030003060080080406018118121100030600800804060181181211000306008008040601811812110003060080080406018118121100121816116121100003000306008008040601811812110003060080080406018118121100030600800804060181181211001218161161211000030003060080080406018118121100030600800804060181181211001218161161211000030003060080080406018118121100121816116121100"

class Page:
    def __init__(self, page_size=4, delta=1):
        self.page_size = page_size
        self.delta = delta
        self.timer = 0
        self.cleans = 0
        self.sum_working_set = 0
        self.max_working_set = 0
        # Array of pages [page, reference bit, n-bit structure]
        self.page = []
        self.page_faults = 0
        self.total_hits = 0
    
    def hit(self, reference):
        self.total_hits += 1
        #print reference, self.page
        if self.timer >= self.delta:
            self.clean()
        self.timer += 1
        h = False
        for p in self.page:
            if p[0] == reference:
                #print "Accessing", reference
                p[1] = 1
                h = True
        if not h:
            self.page_fault()
            self.page += [[reference, 1, 0]]
            self.page_faults += 1
            #print "Loading", reference

    def page_fault(self):
        pass

    def clean_pages(self):
        i = 0
        while i < len(self.page):
            p = self.page[i]
            if p[2] == 0:
                # Remove page from list
                #print "Removing from working set:", p
                self.page.remove(p)
                i = -1
            i += 1

    def update_pages(self):
        working_set_size = 0

        # Update all the n-bit structures
        for p in self.page:
            p[2] = (p[2] << 1) % (1 << 0)
            p[2] = p[2] | p[1]
            p[1] = 0
            if p[2] != 0:
                working_set_size += 1
        
        self.cleans += 1
        self.sum_working_set += working_set_size
        self.timer = 0

        if working_set_size > self.max_working_set:
            self.max_working_set = working_set_size
    
    def clean(self):
        """Goes through and updates all page reference and n-bit structures
        Returns the working set size
        """
        self.update_pages()

        # Remove any that don't belong anymore
        #print "Working set after update:", self.page
        self.clean_pages()
        #print self.page

    def get_total_page_faults(self):
        return self.page_faults
    def get_average_working_set_size(self, string_length):
        return self.sum_working_set*1.0/self.cleans

class PracticalPage(Page):
    def clean(self):
        self.update_pages()

    def page_fault(self):
        self.clean_pages()
        


def parse_string(input_tuple, loop1, page=Page()):
    g = input_tuple[0]
    string_size = 0
    total_string = ""
    # If the string contains an n, we need to loop
    if type(g) is str and g.find("n") != -1:
        n = loop1
        limit = eval(g)
        for q in range(0, limit):
            #print "input_tuple:", input_tuple
            string_size_t, total_string_t = parse_string(input_tuple[1:], loop1-q, page)
            string_size += string_size_t
            total_string += total_string_t
    else:
        for e in input_tuple:
            if type(e) is tuple:
                string_size_t, total_string_t = parse_string(e, loop1, page)
                string_size += string_size_t
                total_string += total_string_t     
            else:
                for c in e:
                    page.hit(c)
                    #print page.get_total_page_faults()
                    #print page.page
                    #print c,
                    string_size += 1
                    total_string += c
    return string_size, total_string

results = []

def calculate_opt_page_faults(input_string, num_frames):
    frames = []
    page_faults = 0
    for i, c in enumerate(input_string):
        if c in frames:
            continue
        if len(frames) < num_frames:
            frames += [c]
        else:
            # Search for the next one to be least used
            next_used = 0
            distance = 0
            for idx,e in enumerate(frames):
                count = 0
                for k in input_string[i:]:
                    if k == e:
                        if count > distance:
                            next_used = idk
                            distance = count
                        break
            # And replace
            frames[next_used] = c
            page_faults += 1
    return page_faults

for i in range(1, 65):
    my_page = Page(delta=i)
    #parse_string(reference_string, 10, my_page)
    size, total_string = parse_string(reference_string, 10, my_page)

    my_page2 = PracticalPage(delta=i)
    parse_string(reference_string, 10, my_page2)

    opt_replaces = calculate_opt_page_faults(expanded_string, my_page.max_working_set)
    opt_replaces2 = calculate_opt_page_faults(expanded_string, my_page2.max_working_set)

    #print size

    results += [(i, my_page.get_total_page_faults(),
                 my_page.get_average_working_set_size(size),
                 ((my_page.get_total_page_faults()*1.0)/size),
                 my_page2.get_total_page_faults(),
                 my_page.max_working_set,
                 my_page2.max_working_set,
                 opt_replaces, opt_replaces2)]

print r'\begin{longtable}{l | c c c c }'
print r'& $P(\Delta)$ & $W(\Delta)$ & $F(\Delta)$ \\ \hline'
for result in results:
    print "%s & %s & %0.2f & %0.2f \\\\" % (
        result[0],
        result[1],
        result[2],
        result[3]
    )
print "\end{longtable}"

for result in results:
    print "%s %s %0.2f %0.2f %s %s %s %s %s" % (result[0], result[1],
        result[2], 1.0/result[3], result[4], result[5], result[6], result[7], result[8])
