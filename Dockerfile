FROM centos:7
EXPOSE 8000 9000 10000

COPY _output/qaclana /qaclana/

CMD ["/qaclana/qaclana"]
