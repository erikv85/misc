TEX=pdflatex
FLAGS=--halt-on-error

junk=aux,log,nav,out,pdf,snm,toc

%: %.tex
	$(TEX) $(FLAGS) $@.tex

clean:
	$(RM) *.{$(junk)}
