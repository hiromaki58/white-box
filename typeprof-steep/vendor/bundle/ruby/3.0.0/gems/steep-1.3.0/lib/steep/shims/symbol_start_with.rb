# Steep runs on Ruby 2.6 and it needs a shim of `Symbol#start_with?`

module Shims
  module SymbolStartWith
    def start_with?(*args)
      to_s.start_with?(*args)
    end

    def end_with?(*args)
      to_s.end_with?(*args)
    end
  end

  unless Symbol.method_defined?(:start_with?)
    Symbol.include(SymbolStartWith)
  end
end

