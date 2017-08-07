FROM scratch
EXPOSE 8000 9000 10000

ADD _output/qaclana /

CMD ["/qaclana"]
