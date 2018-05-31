FROM centos as evaluator
VOLUME /output
COPY evalb /evalb
COPY new.prm /new.prm
COPY heb-ctrees.gold /heb-ctrees.gold
COPY test1.parsed /test1.parsed 
RUN /evalb -p /new.prm /heb-ctrees.gold /test1.parsed > /output.eval