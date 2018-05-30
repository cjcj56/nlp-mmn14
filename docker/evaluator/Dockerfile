FROM centos as evaluator
COPY evalb /evalb
COPY new.prm /new.prm
COPY heb-ctrees.gold /heb-ctrees.gold
COPY test2.parsed /test2.parsed 
RUN /evalb -p /new.prm /heb-ctrees.gold /test2.parsed > /output.eval